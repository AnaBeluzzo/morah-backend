package br.com.morah.service;

import br.com.morah.dto.AutorizacaoRetiradaCreateRequest;
import br.com.morah.dto.EncomendaCreateRequest;
import br.com.morah.dto.EncomendaPage;
import br.com.morah.dto.OcorrenciaCreateRequest;
import br.com.morah.dto.PageMetadata;
import br.com.morah.dto.RetiradaConfirmRequest;
import br.com.morah.dto.StatusAutorizacaoRetirada;
import br.com.morah.dto.StatusEncomenda;
import br.com.morah.dto.StatusOcorrencia;
import br.com.morah.exception.ApiException;
import br.com.morah.model.AutorizacaoRetirada;
import br.com.morah.model.Encomenda;
import br.com.morah.model.Ocorrencia;
import br.com.morah.model.Unidade;
import br.com.morah.pattern.GeradorCodigo;
import br.com.morah.pattern.NotificacaoEncomenda;
import br.com.morah.repository.AutorizacaoRetiradaRepository;
import br.com.morah.repository.EncomendaRepository;
import br.com.morah.repository.OcorrenciaRepository;
import br.com.morah.repository.PessoaPerfilRepository;
import br.com.morah.repository.UnidadeRepository;
import br.com.morah.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class EncomendaService {

    private static final int VALIDADE_PADRAO_HORAS = 24;

    private final EncomendaRepository encomendaRepository;
    private final AutorizacaoRetiradaRepository autorizacaoRetiradaRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final UnidadeRepository unidadeRepository;
    private final PessoaPerfilRepository pessoaPerfilRepository;
    private final NotificacaoEncomenda notificacaoEncomenda;

    public EncomendaService(EncomendaRepository encomendaRepository,
                            AutorizacaoRetiradaRepository autorizacaoRetiradaRepository,
                            OcorrenciaRepository ocorrenciaRepository, UnidadeRepository unidadeRepository,
                            PessoaPerfilRepository pessoaPerfilRepository, NotificacaoEncomenda notificacaoEncomenda) {
        this.encomendaRepository = encomendaRepository;
        this.autorizacaoRetiradaRepository = autorizacaoRetiradaRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.unidadeRepository = unidadeRepository;
        this.pessoaPerfilRepository = pessoaPerfilRepository;
        this.notificacaoEncomenda = notificacaoEncomenda;
    }

    /** Morador/proprietário: a própria unidade; portaria e síndico: o condomínio. */
    @Transactional(readOnly = true)
    public EncomendaPage buscarEncomendas(UsuarioLogado usuario, StatusEncomenda status, int page, int size) {
        Long unidadeId = usuario.isMoradorOuProprietario() ? usuario.unidadeId() : null;
        Page<Encomenda> pagina = encomendaRepository.buscarComFiltros(usuario.condominioId(), unidadeId, status,
                PageRequest.of(page, size));
        return new EncomendaPage(pagina.getContent().stream().map(br.com.morah.dto.Encomenda::de).toList(),
                PageMetadata.de(pagina));
    }

    /** Nasce aguardando retirada, com código de 6 dígitos; os moradores da unidade são avisados. */
    @Transactional
    public br.com.morah.dto.Encomenda criarEncomenda(UsuarioLogado usuario, EncomendaCreateRequest request) {
        Unidade unidade = unidadeRepository.findByIdAndBlocoCondominioId(request.unidadeId(), usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Unidade não encontrada"));

        Encomenda encomenda = new Encomenda();
        encomenda.setUnidade(unidade);
        encomenda.setRemetente(request.remetente());
        encomenda.setTransportadora(request.transportadora());
        encomenda.setCodigoRastreio(request.codigoRastreio());
        encomenda.setFotoUrl(request.fotoUrl());
        encomenda.setCodigoRetirada(GeradorCodigo.getInstance().gerarCodigo());
        encomenda.setRecebidaEm(Instant.now());
        encomenda.setStatus(StatusEncomenda.AGUARDANDO_RETIRADA);
        encomendaRepository.save(encomenda);

        notificacaoEncomenda.notificar(encomenda, pessoaPerfilRepository.findMoradoresByUnidadeId(unidade.getId()));
        return br.com.morah.dto.Encomenda.de(encomenda);
    }

    @Transactional(readOnly = true)
    public br.com.morah.dto.Encomenda buscarEncomenda(UsuarioLogado usuario, Long encomendaId) {
        return br.com.morah.dto.Encomenda.de(buscarDoCondominio(usuario, encomendaId));
    }

    @Transactional
    public br.com.morah.dto.AutorizacaoRetirada autorizarRetiradaTerceiro(UsuarioLogado usuario, Long encomendaId,
                                                                          AutorizacaoRetiradaCreateRequest request) {
        Encomenda encomenda = buscarDoCondominio(usuario, encomendaId);
        usuario.verificarUnidade(encomenda.getUnidade().getId());

        int horas = request.validadeHoras() == null ? VALIDADE_PADRAO_HORAS : request.validadeHoras();
        AutorizacaoRetirada autorizacao = new AutorizacaoRetirada();
        autorizacao.setEncomenda(encomenda);
        autorizacao.setNomeTerceiro(request.nomeTerceiro());
        autorizacao.setDocumento(request.documento());
        autorizacao.setCodigo(GeradorCodigo.getInstance().gerarCodigo());
        autorizacao.setValidade(Instant.now().plus(Duration.ofHours(horas)));
        autorizacao.setStatus(StatusAutorizacaoRetirada.ATIVA);
        return br.com.morah.dto.AutorizacaoRetirada.de(autorizacaoRetiradaRepository.save(autorizacao));
    }

    /** Aceita o código da encomenda ou o de uma autorização de terceiro ativa e válida (que vira "utilizada"). */
    @Transactional
    public br.com.morah.dto.Encomenda confirmarRetirada(UsuarioLogado usuario, Long encomendaId, RetiradaConfirmRequest request) {
        Encomenda encomenda = buscarDoCondominio(usuario, encomendaId);
        if (encomenda.getStatus() != StatusEncomenda.AGUARDANDO_RETIRADA) {
            throw ApiException.conflito("Encomenda já retirada ou extraviada");
        }

        if (!request.codigo().equals(encomenda.getCodigoRetirada())) {
            Optional<AutorizacaoRetirada> autorizacao = autorizacaoRetiradaRepository
                    .findFirstByEncomendaIdAndCodigoAndStatus(encomendaId, request.codigo(), StatusAutorizacaoRetirada.ATIVA)
                    .filter(a -> a.calcularStatus() == StatusAutorizacaoRetirada.ATIVA);
            if (autorizacao.isEmpty()) {
                throw ApiException.conflito("Código de retirada inválido ou expirado");
            }
            autorizacao.get().setStatus(StatusAutorizacaoRetirada.UTILIZADA);
        }

        encomenda.setStatus(StatusEncomenda.RETIRADA);
        encomenda.setRetiradaEm(Instant.now());
        return br.com.morah.dto.Encomenda.de(encomenda);
    }

    /** Cria a ocorrência aberta e marca a encomenda como extraviada. */
    @Transactional
    public br.com.morah.dto.Ocorrencia abrirOcorrencia(UsuarioLogado usuario, Long encomendaId, OcorrenciaCreateRequest request) {
        Encomenda encomenda = buscarDoCondominio(usuario, encomendaId);
        usuario.verificarUnidade(encomenda.getUnidade().getId());

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setCondominio(encomenda.getUnidade().getBloco().getCondominio());
        ocorrencia.setEncomenda(encomenda);
        ocorrencia.setTitulo(request.titulo());
        ocorrencia.setDescricao(request.descricao());
        ocorrencia.setStatus(StatusOcorrencia.ABERTA);
        ocorrencia.setAbertaEm(Instant.now());
        ocorrenciaRepository.save(ocorrencia);

        encomenda.setStatus(StatusEncomenda.EXTRAVIADA);
        return br.com.morah.dto.Ocorrencia.de(ocorrencia);
    }

    private Encomenda buscarDoCondominio(UsuarioLogado usuario, Long encomendaId) {
        return encomendaRepository.findByIdAndUnidadeBlocoCondominioId(encomendaId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Encomenda não encontrada"));
    }
}
