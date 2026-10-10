package br.com.morah.service;

import br.com.morah.dto.AvisoCreateRequest;
import br.com.morah.dto.AvisoPage;
import br.com.morah.dto.AvisoUpdateRequest;
import br.com.morah.dto.PageMetadata;
import br.com.morah.dto.PrioridadeAviso;
import br.com.morah.dto.TipoPublicoAlvo;
import br.com.morah.exception.ApiException;
import br.com.morah.model.Aviso;
import br.com.morah.model.LeituraAviso;
import br.com.morah.model.Pessoa;
import br.com.morah.model.PessoaPerfil;
import br.com.morah.model.PublicoAlvo;
import br.com.morah.pattern.NotificacaoAviso;
import br.com.morah.repository.AvisoRepository;
import br.com.morah.repository.BlocoRepository;
import br.com.morah.repository.LeituraAvisoRepository;
import br.com.morah.repository.PessoaPerfilRepository;
import br.com.morah.repository.PessoaRepository;
import br.com.morah.repository.UnidadeRepository;
import br.com.morah.security.UsuarioLogado;
import br.com.morah.repository.CondominioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AvisoService {

    private final AvisoRepository avisoRepository;
    private final LeituraAvisoRepository leituraAvisoRepository;
    private final UnidadeRepository unidadeRepository;
    private final BlocoRepository blocoRepository;
    private final PessoaRepository pessoaRepository;
    private final PessoaPerfilRepository pessoaPerfilRepository;
    private final NotificacaoAviso notificacaoAviso;
    private final CondominioRepository condominioRepository;

    public AvisoService(AvisoRepository avisoRepository, LeituraAvisoRepository leituraAvisoRepository,
                        UnidadeRepository unidadeRepository, BlocoRepository blocoRepository,
                        PessoaRepository pessoaRepository, PessoaPerfilRepository pessoaPerfilRepository,
                        NotificacaoAviso notificacaoAviso, CondominioRepository condominioRepository) {
        this.avisoRepository = avisoRepository;
        this.leituraAvisoRepository = leituraAvisoRepository;
        this.unidadeRepository = unidadeRepository;
        this.blocoRepository = blocoRepository;
        this.pessoaRepository = pessoaRepository;
        this.pessoaPerfilRepository = pessoaPerfilRepository;
        this.notificacaoAviso = notificacaoAviso;
        this.condominioRepository = condominioRepository;
    }

    @Transactional(readOnly = true)
    public AvisoPage buscarAvisos(UsuarioLogado usuario, String categoria, PrioridadeAviso prioridade, int page, int size) {
        boolean todos = !usuario.isMoradorOuProprietario();
        Long blocoId = todos ? null : blocoDaUnidade(usuario.unidadeId());
        Page<Aviso> pagina = avisoRepository.buscarVigentes(usuario.condominioId(), Instant.now(), categoria, prioridade,
                todos, blocoId, usuario.unidadeId(), PageRequest.of(page, size));
        List<br.com.morah.dto.Aviso> avisos = pagina.getContent().stream()
                .map(aviso -> paraDto(aviso, usuario))
                .toList();
        return new AvisoPage(avisos, PageMetadata.de(pagina));
    }

    @Transactional
    public br.com.morah.dto.Aviso criarAviso(UsuarioLogado usuario, AvisoCreateRequest request) {
        validarPublicoAlvo(usuario, request.publicoAlvo());

        PublicoAlvo publico = new PublicoAlvo();
        publico.setTipo(request.publicoAlvo().tipo());
        publico.setBlocoId(request.publicoAlvo().blocoId());
        publico.setUnidadeId(request.publicoAlvo().unidadeId());

        Aviso aviso = new Aviso();
        aviso.setCondominio(condominioRepository.getReferenceById(usuario.condominioId()));
        aviso.setTitulo(request.titulo());
        aviso.setConteudo(request.conteudo());
        aviso.setCategoria(request.categoria());
        aviso.setPrioridade(request.prioridade() == null ? PrioridadeAviso.NORMAL : request.prioridade());
        aviso.setPublicoAlvo(publico);
        // autor e data de publicação são preenchidos pelo servidor
        aviso.setAutor(pessoaRepository.findById(usuario.pessoaId()).orElseThrow());
        aviso.setPublicadoEm(Instant.now());
        aviso.setExpiraEm(request.expiraEm());
        avisoRepository.save(aviso);

        notificacaoAviso.notificar(aviso, destinatarios(aviso, usuario));
        return paraDto(aviso, usuario);
    }

    /** GET de um aviso: 404 de outro condomínio, 410 se expirado; marca como lido. */
    @Transactional
    public br.com.morah.dto.Aviso buscarAviso(UsuarioLogado usuario, Long avisoId) {
        Aviso aviso = buscarDoCondominio(usuario, avisoId);
        if (!aviso.isVigente()) {
            throw ApiException.expirado("Aviso fora de vigência");
        }
        if (!leituraAvisoRepository.existsByAvisoIdAndPessoaId(avisoId, usuario.pessoaId())) {
            LeituraAviso leitura = new LeituraAviso();
            leitura.setAviso(aviso);
            leitura.setPessoa(pessoaRepository.findById(usuario.pessoaId()).orElseThrow());
            leitura.setLidoEm(Instant.now());
            leituraAvisoRepository.save(leitura);
        }
        return br.com.morah.dto.Aviso.de(aviso, true);
    }

    @Transactional
    public br.com.morah.dto.Aviso alterarAviso(UsuarioLogado usuario, Long avisoId, AvisoUpdateRequest request) {
        Aviso aviso = buscarDoCondominio(usuario, avisoId);
        // PATCH: só altera o que foi enviado
        if (request.titulo() != null) {
            aviso.setTitulo(request.titulo());
        }
        if (request.conteudo() != null) {
            aviso.setConteudo(request.conteudo());
        }
        if (request.categoria() != null) {
            aviso.setCategoria(request.categoria());
        }
        if (request.prioridade() != null) {
            aviso.setPrioridade(request.prioridade());
        }
        if (request.expiraEm() != null) {
            aviso.setExpiraEm(request.expiraEm());
        }
        return paraDto(aviso, usuario);
    }

    /** O aviso não é apagado: expira agora. */
    @Transactional
    public br.com.morah.dto.Aviso deletarAviso(UsuarioLogado usuario, Long avisoId) {
        Aviso aviso = buscarDoCondominio(usuario, avisoId);
        aviso.setExpiraEm(Instant.now());
        return paraDto(aviso, usuario);
    }

    /** Regra de visibilidade do público-alvo (síndico e portaria veem todos). */
    @Transactional(readOnly = true)
    public boolean isVisivel(br.com.morah.dto.PublicoAlvo publico, UsuarioLogado usuario) {
        if (!usuario.isMoradorOuProprietario()) {
            return true;
        }
        return switch (publico.tipo()) {
            case CONDOMINIO -> true;
            case BLOCO -> publico.blocoId().equals(blocoDaUnidade(usuario.unidadeId()));
            case UNIDADE -> publico.unidadeId().equals(usuario.unidadeId());
        };
    }

    private Aviso buscarDoCondominio(UsuarioLogado usuario, Long avisoId) {
        return avisoRepository.findByIdAndCondominioId(avisoId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Aviso não encontrado"));
    }

    private br.com.morah.dto.Aviso paraDto(Aviso aviso, UsuarioLogado usuario) {
        boolean lido = leituraAvisoRepository.existsByAvisoIdAndPessoaId(aviso.getId(), usuario.pessoaId());
        return br.com.morah.dto.Aviso.de(aviso, lido);
    }

    private Long blocoDaUnidade(Long unidadeId) {
        return unidadeRepository.findById(unidadeId).orElseThrow().getBloco().getId();
    }

    /** O bloco/unidade do público-alvo precisa existir no condomínio ativo. */
    private void validarPublicoAlvo(UsuarioLogado usuario, br.com.morah.dto.PublicoAlvo publico) {
        if (publico.tipo() == TipoPublicoAlvo.BLOCO
                && !blocoRepository.existsByIdAndCondominioId(publico.blocoId(), usuario.condominioId())) {
            throw ApiException.requisicaoInvalida("Bloco do público-alvo não existe neste condomínio");
        }
        if (publico.tipo() == TipoPublicoAlvo.UNIDADE
                && unidadeRepository.findByIdAndBlocoCondominioId(publico.unidadeId(), usuario.condominioId()).isEmpty()) {
            throw ApiException.requisicaoInvalida("Unidade do público-alvo não existe neste condomínio");
        }
    }

    /** Pessoas com perfil no público-alvo, sem repetir e sem o próprio autor. */
    private List<Pessoa> destinatarios(Aviso aviso, UsuarioLogado usuario) {
        PublicoAlvo publico = aviso.getPublicoAlvo();
        List<PessoaPerfil> perfis = switch (publico.getTipo()) {
            case CONDOMINIO -> pessoaPerfilRepository.findByCondominioId(usuario.condominioId());
            case BLOCO -> pessoaPerfilRepository.findByUnidadeBlocoId(publico.getBlocoId());
            case UNIDADE -> pessoaPerfilRepository.findByUnidadeId(publico.getUnidadeId());
        };
        return perfis.stream()
                .map(PessoaPerfil::getPessoa)
                .filter(pessoa -> !pessoa.getId().equals(usuario.pessoaId()))
                .distinct()
                .toList();
    }
}
