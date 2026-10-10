package br.com.morah.service;

import br.com.morah.dto.login.Perfil;
import br.com.morah.dto.minhaunidade.DecisaoSolicitacaoVinculo;
import br.com.morah.dto.minhaunidade.DecisaoSolicitacaoVinculoRequest;
import br.com.morah.dto.minhaunidade.InquilinoCreateRequest;
import br.com.morah.dto.minhaunidade.SolicitacaoVinculoCreateRequest;
import br.com.morah.dto.minhaunidade.StatusSolicitacaoVinculo;
import br.com.morah.dto.minhaunidade.TipoVinculo;
import br.com.morah.exception.ApiException;
import br.com.morah.model.condominio.Unidade;
import br.com.morah.model.login.Pessoa;
import br.com.morah.model.login.PessoaPerfil;
import br.com.morah.model.minhaunidade.SolicitacaoVinculo;
import br.com.morah.model.minhaunidade.VinculoUnidade;
import br.com.morah.pattern.templatemethod.notificacao.NotificacaoConvite;
import br.com.morah.repository.condominio.CondominioRepository;
import br.com.morah.repository.condominio.UnidadeRepository;
import br.com.morah.repository.login.PessoaPerfilRepository;
import br.com.morah.repository.login.PessoaRepository;
import br.com.morah.repository.login.SessaoRepository;
import br.com.morah.repository.minhaunidade.SolicitacaoVinculoRepository;
import br.com.morah.repository.minhaunidade.VinculoUnidadeRepository;
import br.com.morah.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;
    private final VinculoUnidadeRepository vinculoUnidadeRepository;
    private final SolicitacaoVinculoRepository solicitacaoVinculoRepository;
    private final PessoaRepository pessoaRepository;
    private final PessoaPerfilRepository pessoaPerfilRepository;
    private final SessaoRepository sessaoRepository;
    private final CondominioRepository condominioRepository;
    private final NotificacaoConvite notificacaoConvite;

    public UnidadeService(UnidadeRepository unidadeRepository, VinculoUnidadeRepository vinculoUnidadeRepository,
                          SolicitacaoVinculoRepository solicitacaoVinculoRepository, PessoaRepository pessoaRepository,
                          PessoaPerfilRepository pessoaPerfilRepository, SessaoRepository sessaoRepository,
                          CondominioRepository condominioRepository, NotificacaoConvite notificacaoConvite) {
        this.unidadeRepository = unidadeRepository;
        this.vinculoUnidadeRepository = vinculoUnidadeRepository;
        this.solicitacaoVinculoRepository = solicitacaoVinculoRepository;
        this.pessoaRepository = pessoaRepository;
        this.pessoaPerfilRepository = pessoaPerfilRepository;
        this.sessaoRepository = sessaoRepository;
        this.condominioRepository = condominioRepository;
        this.notificacaoConvite = notificacaoConvite;
    }

    @Transactional(readOnly = true)
    public br.com.morah.dto.minhaunidade.Unidade buscarMinhaUnidade(UsuarioLogado usuario) {
        return br.com.morah.dto.minhaunidade.Unidade.de(buscarDoCondominio(usuario, usuario.unidadeId()));
    }

    @Transactional(readOnly = true)
    public List<br.com.morah.dto.minhaunidade.VinculoUnidade> buscarVinculos(UsuarioLogado usuario) {
        return vinculoUnidadeRepository.findByUnidadeIdOrderById(usuario.unidadeId()).stream()
                .map(br.com.morah.dto.minhaunidade.VinculoUnidade::de)
                .toList();
    }

    @Transactional
    public br.com.morah.dto.minhaunidade.SolicitacaoVinculo solicitarVinculo(UsuarioLogado usuario, SolicitacaoVinculoCreateRequest request) {
        SolicitacaoVinculo solicitacao = new SolicitacaoVinculo();
        solicitacao.setUnidade(buscarDoCondominio(usuario, usuario.unidadeId()));
        solicitacao.setTipoVinculo(request.tipoVinculo());
        solicitacao.setNomeSolicitado(request.nome());
        solicitacao.setCpf(request.cpf());
        solicitacao.setStatus(StatusSolicitacaoVinculo.PENDENTE);
        solicitacao.setSolicitadoEm(Instant.now());
        return br.com.morah.dto.minhaunidade.SolicitacaoVinculo.de(solicitacaoVinculoRepository.save(solicitacao));
    }

    /** Cria a pessoa, o vínculo "inquilino" e o perfil "morador"; o convite vai por e-mail. */
    @Transactional
    public br.com.morah.dto.minhaunidade.VinculoUnidade criarInquilino(UsuarioLogado usuario, InquilinoCreateRequest request) {
        if (request.contratoFim() != null && request.contratoFim().isBefore(request.contratoInicio())) {
            throw ApiException.requisicaoInvalida("contratoFim não pode ser anterior a contratoInicio");
        }
        if (request.email() != null && pessoaRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.requisicaoInvalida("Já existe uma pessoa cadastrada com este e-mail");
        }
        Unidade unidade = buscarDoCondominio(usuario, usuario.unidadeId());

        Pessoa inquilino = new Pessoa();
        inquilino.setNome(request.nome());
        inquilino.setCpf(request.cpf());
        inquilino.setEmail(request.email());
        inquilino.setTelefone(request.telefone());
        pessoaRepository.save(inquilino);

        VinculoUnidade vinculo = new VinculoUnidade();
        vinculo.setUnidade(unidade);
        vinculo.setPessoa(inquilino);
        vinculo.setTipoVinculo(TipoVinculo.INQUILINO);
        vinculo.setPrincipal(false);
        vinculo.setInicio(request.contratoInicio());
        vinculo.setFim(request.contratoFim());
        vinculoUnidadeRepository.save(vinculo);

        PessoaPerfil perfil = new PessoaPerfil();
        perfil.setPessoa(inquilino);
        perfil.setPerfil(Perfil.MORADOR);
        perfil.setCondominio(condominioRepository.getReferenceById(usuario.condominioId()));
        perfil.setUnidade(unidade);
        pessoaPerfilRepository.save(perfil);

        notificacaoConvite.notificar(vinculo, List.of(inquilino));
        return br.com.morah.dto.minhaunidade.VinculoUnidade.de(vinculo);
    }

    /** Fim do contrato = hoje; remove o perfil do inquilino e encerra as sessões dele. */
    @Transactional
    public void deletarInquilino(UsuarioLogado usuario, Long vinculoId) {
        VinculoUnidade vinculo = vinculoUnidadeRepository.findByIdAndUnidadeBlocoCondominioId(vinculoId, usuario.condominioId())
                .filter(v -> v.getTipoVinculo() == TipoVinculo.INQUILINO)
                .orElseThrow(() -> ApiException.naoEncontrado("Inquilino não encontrado"));
        usuario.verificarUnidade(vinculo.getUnidade().getId());

        vinculo.setFim(LocalDate.now());
        Long pessoaId = vinculo.getPessoa().getId();
        pessoaPerfilRepository.deleteByPessoaIdAndUnidadeIdAndPerfil(pessoaId, vinculo.getUnidade().getId(), Perfil.MORADOR);
        sessaoRepository.deleteByPessoaId(pessoaId);
    }

    @Transactional(readOnly = true)
    public br.com.morah.dto.minhaunidade.Unidade buscarUnidade(UsuarioLogado usuario, Long unidadeId) {
        return br.com.morah.dto.minhaunidade.Unidade.de(buscarDoCondominio(usuario, unidadeId));
    }

    @Transactional(readOnly = true)
    public List<br.com.morah.dto.minhaunidade.SolicitacaoVinculo> buscarSolicitacoesVinculo(UsuarioLogado usuario, Long unidadeId) {
        buscarDoCondominio(usuario, unidadeId);
        return solicitacaoVinculoRepository.findByUnidadeIdAndStatusOrderById(unidadeId, StatusSolicitacaoVinculo.PENDENTE)
                .stream().map(br.com.morah.dto.minhaunidade.SolicitacaoVinculo::de).toList();
    }

    /** Aprovar cria a pessoa e o vínculo com início hoje; solicitação já decidida → 409. */
    @Transactional
    public br.com.morah.dto.minhaunidade.SolicitacaoVinculo decidirSolicitacaoVinculo(UsuarioLogado usuario, Long unidadeId,
                                                                          Long solicitacaoId,
                                                                          DecisaoSolicitacaoVinculoRequest request) {
        Unidade unidade = buscarDoCondominio(usuario, unidadeId);
        SolicitacaoVinculo solicitacao = solicitacaoVinculoRepository.findByIdAndUnidadeId(solicitacaoId, unidadeId)
                .orElseThrow(() -> ApiException.naoEncontrado("Solicitação não encontrada"));
        if (solicitacao.getStatus() != StatusSolicitacaoVinculo.PENDENTE) {
            throw ApiException.conflito("Solicitação já decidida");
        }

        if (request.decisao() == DecisaoSolicitacaoVinculo.APROVADA) {
            Pessoa pessoa = new Pessoa();
            pessoa.setNome(solicitacao.getNomeSolicitado());
            pessoa.setCpf(solicitacao.getCpf());
            pessoaRepository.save(pessoa);

            VinculoUnidade vinculo = new VinculoUnidade();
            vinculo.setUnidade(unidade);
            vinculo.setPessoa(pessoa);
            vinculo.setTipoVinculo(solicitacao.getTipoVinculo());
            vinculo.setPrincipal(false);
            vinculo.setInicio(LocalDate.now());
            vinculoUnidadeRepository.save(vinculo);
            solicitacao.setStatus(StatusSolicitacaoVinculo.APROVADA);
        } else {
            solicitacao.setStatus(StatusSolicitacaoVinculo.REJEITADA);
        }
        return br.com.morah.dto.minhaunidade.SolicitacaoVinculo.de(solicitacao);
    }

    /** Unidade de outro condomínio (ou inexistente) → 404. */
    private Unidade buscarDoCondominio(UsuarioLogado usuario, Long unidadeId) {
        return unidadeRepository.findByIdAndBlocoCondominioId(unidadeId, usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Unidade não encontrada"));
    }
}
