package br.com.morah.service;

import br.com.morah.dto.AcessoCreateRequest;
import br.com.morah.dto.AcessoPage;
import br.com.morah.dto.AutorizacaoVisitaCreateRequest;
import br.com.morah.dto.AutorizacaoVisitaPage;
import br.com.morah.dto.DecisaoAutorizacao;
import br.com.morah.dto.DecisaoAutorizacaoRequest;
import br.com.morah.dto.PageMetadata;
import br.com.morah.dto.StatusAcesso;
import br.com.morah.dto.StatusAutorizacaoVisita;
import br.com.morah.dto.TipoAcesso;
import br.com.morah.exception.ApiException;
import br.com.morah.model.Acesso;
import br.com.morah.model.AutorizacaoVisita;
import br.com.morah.model.Unidade;
import br.com.morah.model.Visitante;
import br.com.morah.pattern.NotificacaoVisitante;
import br.com.morah.repository.AcessoRepository;
import br.com.morah.repository.AutorizacaoVisitaRepository;
import br.com.morah.repository.CondominioRepository;
import br.com.morah.repository.PessoaPerfilRepository;
import br.com.morah.repository.UnidadeRepository;
import br.com.morah.repository.VisitanteRepository;
import br.com.morah.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class PortariaService {

    private final VisitanteRepository visitanteRepository;
    private final AutorizacaoVisitaRepository autorizacaoVisitaRepository;
    private final AcessoRepository acessoRepository;
    private final UnidadeRepository unidadeRepository;
    private final CondominioRepository condominioRepository;
    private final PessoaPerfilRepository pessoaPerfilRepository;
    private final NotificacaoVisitante notificacaoVisitante;

    public PortariaService(VisitanteRepository visitanteRepository, AutorizacaoVisitaRepository autorizacaoVisitaRepository,
                           AcessoRepository acessoRepository, UnidadeRepository unidadeRepository,
                           CondominioRepository condominioRepository, PessoaPerfilRepository pessoaPerfilRepository,
                           NotificacaoVisitante notificacaoVisitante) {
        this.visitanteRepository = visitanteRepository;
        this.autorizacaoVisitaRepository = autorizacaoVisitaRepository;
        this.acessoRepository = acessoRepository;
        this.unidadeRepository = unidadeRepository;
        this.condominioRepository = condominioRepository;
        this.pessoaPerfilRepository = pessoaPerfilRepository;
        this.notificacaoVisitante = notificacaoVisitante;
    }

    /** Cria o visitante e a autorização pendente, e avisa os moradores da unidade. */
    @Transactional
    public br.com.morah.dto.AutorizacaoVisita registrarVisitante(UsuarioLogado usuario, AutorizacaoVisitaCreateRequest request) {
        Unidade unidade = unidadeRepository.findByIdAndBlocoCondominioId(request.unidadeId(), usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Unidade não encontrada"));

        Visitante visitante = new Visitante();
        visitante.setCondominio(condominioRepository.getReferenceById(usuario.condominioId()));
        visitante.setNome(request.visitante().nome());
        visitante.setDocumento(request.visitante().documento());
        visitante.setTelefone(request.visitante().telefone());
        visitanteRepository.save(visitante);

        AutorizacaoVisita autorizacao = new AutorizacaoVisita();
        autorizacao.setVisitante(visitante);
        autorizacao.setUnidade(unidade);
        autorizacao.setMotivo(request.motivo());
        autorizacao.setSolicitadoEm(Instant.now());
        autorizacao.setStatus(StatusAutorizacaoVisita.PENDENTE);
        autorizacaoVisitaRepository.save(autorizacao);

        notificacaoVisitante.notificar(autorizacao, pessoaPerfilRepository.findMoradoresByUnidadeId(unidade.getId()));
        return br.com.morah.dto.AutorizacaoVisita.de(autorizacao);
    }

    /** Portaria vê as do condomínio; morador/proprietário só as da própria unidade. */
    @Transactional(readOnly = true)
    public AutorizacaoVisitaPage buscarAutorizacoes(UsuarioLogado usuario, StatusAutorizacaoVisita status, int page, int size) {
        Long unidadeId = usuario.isMoradorOuProprietario() ? usuario.unidadeId() : null;

        // "pendente" e "expirada" são o mesmo status gravado; o horário da solicitação separa os dois
        Instant limite = Instant.now().minus(Duration.ofMinutes(AutorizacaoVisita.MINUTOS_EXPIRACAO_AUTORIZACAO));
        StatusAutorizacaoVisita statusGravado = status == StatusAutorizacaoVisita.EXPIRADA ? StatusAutorizacaoVisita.PENDENTE : status;
        Instant solicitadoAntesDe = status == StatusAutorizacaoVisita.EXPIRADA ? limite : null;
        Instant solicitadoAPartirDe = status == StatusAutorizacaoVisita.PENDENTE ? limite : null;

        Page<AutorizacaoVisita> pagina = autorizacaoVisitaRepository.buscarComFiltros(usuario.condominioId(), unidadeId,
                statusGravado, solicitadoAntesDe, solicitadoAPartirDe, PageRequest.of(page, size));
        return new AutorizacaoVisitaPage(pagina.getContent().stream().map(br.com.morah.dto.AutorizacaoVisita::de).toList(),
                PageMetadata.de(pagina));
    }

    @Transactional(readOnly = true)
    public br.com.morah.dto.AutorizacaoVisita buscarAutorizacao(UsuarioLogado usuario, Long autorizacaoId) {
        return br.com.morah.dto.AutorizacaoVisita.de(buscarAutorizacaoPermitida(usuario, autorizacaoId));
    }

    /** Só a unidade da autorização decide; expirada ou já decidida → 409. */
    @Transactional
    public br.com.morah.dto.AutorizacaoVisita decidirAutorizacao(UsuarioLogado usuario, Long autorizacaoId,
                                                                 DecisaoAutorizacaoRequest request) {
        AutorizacaoVisita autorizacao = buscarAutorizacaoPermitida(usuario, autorizacaoId);
        StatusAutorizacaoVisita atual = autorizacao.calcularStatus();
        if (atual == StatusAutorizacaoVisita.EXPIRADA) {
            throw ApiException.conflito("Autorização expirada");
        }
        if (atual != StatusAutorizacaoVisita.PENDENTE) {
            throw ApiException.conflito("Autorização já decidida");
        }
        autorizacao.setStatus(request.decisao() == DecisaoAutorizacao.AUTORIZADO
                ? StatusAutorizacaoVisita.AUTORIZADA : StatusAutorizacaoVisita.RECUSADA);
        autorizacao.setRespondidoEm(Instant.now());
        return br.com.morah.dto.AutorizacaoVisita.de(autorizacao);
    }

    @Transactional
    public void reenviarAutorizacao(UsuarioLogado usuario, Long autorizacaoId) {
        AutorizacaoVisita autorizacao = buscarAutorizacaoPermitida(usuario, autorizacaoId);
        if (autorizacao.calcularStatus() != StatusAutorizacaoVisita.PENDENTE) {
            throw ApiException.conflito("A autorização não está mais pendente");
        }
        notificacaoVisitante.notificar(autorizacao,
                pessoaPerfilRepository.findMoradoresByUnidadeId(autorizacao.getUnidade().getId()));
    }

    /** Histórico do condomínio; com "data", só as entradas daquele dia (fuso do servidor). */
    @Transactional(readOnly = true)
    public AcessoPage buscarAcessos(UsuarioLogado usuario, LocalDate data, int page, int size) {
        Instant inicio = data == null ? null : data.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant fim = data == null ? null : data.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Page<Acesso> pagina = acessoRepository.buscarDoCondominio(usuario.condominioId(), inicio, fim, PageRequest.of(page, size));
        return new AcessoPage(pagina.getContent().stream().map(br.com.morah.dto.Acesso::de).toList(), PageMetadata.de(pagina));
    }

    /** "entrada" abre um acesso; "saida" fecha o último acesso aberto do visitante. */
    @Transactional
    public br.com.morah.dto.Acesso registrarAcesso(UsuarioLogado usuario, AcessoCreateRequest request) {
        Visitante visitante = visitanteRepository.findByIdAndCondominioId(request.visitanteId(), usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Visitante não encontrado"));

        if (request.tipo() == TipoAcesso.SAIDA) {
            Acesso aberto = acessoRepository.findFirstByVisitanteIdAndStatusOrderByEntradaDesc(visitante.getId(), StatusAcesso.NO_CONDOMINIO)
                    .orElseThrow(() -> ApiException.conflito("Saída sem entrada aberta para este visitante"));
            aberto.setSaida(Instant.now());
            aberto.setTipo(TipoAcesso.SAIDA);
            aberto.setStatus(StatusAcesso.SAIU);
            return br.com.morah.dto.Acesso.de(aberto);
        }

        Acesso acesso = new Acesso();
        if (request.autorizacaoId() != null) {
            AutorizacaoVisita autorizacao = autorizacaoVisitaRepository
                    .findByIdAndUnidadeBlocoCondominioId(request.autorizacaoId(), usuario.condominioId())
                    .orElseThrow(() -> ApiException.naoEncontrado("Autorização não encontrada"));
            if (autorizacao.calcularStatus() != StatusAutorizacaoVisita.AUTORIZADA
                    || !autorizacao.getVisitante().getId().equals(visitante.getId())) {
                throw ApiException.conflito("A autorização informada não está autorizada para este visitante");
            }
            acesso.setAutorizacao(autorizacao);
        }
        acesso.setVisitante(visitante);
        acesso.setEntrada(Instant.now());
        acesso.setTipo(TipoAcesso.ENTRADA);
        acesso.setStatus(StatusAcesso.NO_CONDOMINIO);
        return br.com.morah.dto.Acesso.de(acessoRepository.save(acesso));
    }

    /** 404 se for de outro condomínio; 403 se morador/proprietário de outra unidade. */
    private AutorizacaoVisita buscarAutorizacaoPermitida(UsuarioLogado usuario, Long autorizacaoId) {
        AutorizacaoVisita autorizacao = autorizacaoVisitaRepository.findByIdAndUnidadeBlocoCondominioId(autorizacaoId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Autorização não encontrada"));
        usuario.verificarUnidade(autorizacao.getUnidade().getId());
        return autorizacao;
    }
}
