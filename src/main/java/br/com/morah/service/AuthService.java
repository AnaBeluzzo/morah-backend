package br.com.morah.service;

import br.com.morah.dto.login.ContextoAtivo;
import br.com.morah.dto.login.ContextoAtivoResponse;
import br.com.morah.dto.login.ContextoRequest;
import br.com.morah.dto.login.LoginRequest;
import br.com.morah.dto.login.LoginResponse;
import br.com.morah.dto.login.MeResponse;
import br.com.morah.dto.login.Perfil;
import br.com.morah.dto.login.PerfilDisponivel;
import br.com.morah.dto.login.PessoaAutenticada;
import br.com.morah.dto.login.RefreshRequest;
import br.com.morah.exception.ApiException;
import br.com.morah.model.condominio.Condominio;
import br.com.morah.model.condominio.Unidade;
import br.com.morah.model.login.Pessoa;
import br.com.morah.model.login.PessoaPerfil;
import br.com.morah.model.login.Sessao;
import br.com.morah.repository.login.PessoaPerfilRepository;
import br.com.morah.repository.login.PessoaRepository;
import br.com.morah.repository.login.SessaoRepository;
import br.com.morah.security.TokenService;
import br.com.morah.security.UsuarioLogado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private static final Duration VALIDADE_REFRESH = Duration.ofDays(7);
    private static final String TIPO_TOKEN = "Bearer";

    private final PessoaRepository pessoaRepository;
    private final PessoaPerfilRepository pessoaPerfilRepository;
    private final SessaoRepository sessaoRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(PessoaRepository pessoaRepository, PessoaPerfilRepository pessoaPerfilRepository,
                       SessaoRepository sessaoRepository, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.pessoaRepository = pessoaRepository;
        this.pessoaPerfilRepository = pessoaPerfilRepository;
        this.sessaoRepository = sessaoRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Pessoa pessoa = pessoaRepository.findByEmailIgnoreCase(request.email())
                .filter(p -> p.getSenhaHash() != null && passwordEncoder.matches(request.senha(), p.getSenhaHash()))
                .orElseThrow(() -> ApiException.naoAutenticado("E-mail ou senha inválidos"));

        List<PessoaPerfil> perfis = pessoaPerfilRepository.findByPessoaIdOrderById(pessoa.getId());
        if (perfis.isEmpty()) {
            throw ApiException.naoAutenticado("A pessoa não possui nenhum perfil de acesso");
        }

        // contexto inicial: o primeiro vínculo da pessoa (menor id)
        PessoaPerfil primeiro = perfis.get(0);
        Sessao sessao = criarSessao(pessoa, primeiro.getPerfil(), primeiro.getCondominio(), primeiro.getUnidade());
        return new LoginResponse(tokenService.gerarAccessToken(sessao), sessao.getRefreshToken(), TIPO_TOKEN,
                TokenService.VALIDADE_SEGUNDOS, ContextoAtivo.de(sessao),
                perfis.stream().map(PerfilDisponivel::de).toList());
    }

    @Transactional(readOnly = true)
    public MeResponse buscarMe(UsuarioLogado usuario) {
        Sessao sessao = buscarSessao(usuario);
        List<PerfilDisponivel> perfis = pessoaPerfilRepository.findByPessoaIdOrderById(usuario.pessoaId()).stream()
                .map(PerfilDisponivel::de).toList();
        return new MeResponse(PessoaAutenticada.de(sessao.getPessoa()), perfis, ContextoAtivo.de(sessao));
    }

    @Transactional
    public ContextoAtivoResponse selecionarContexto(UsuarioLogado usuario, ContextoRequest request) {
        PessoaPerfil vinculo = pessoaPerfilRepository
                .findFirstByPessoaIdAndCondominioIdAndPerfilOrderById(usuario.pessoaId(), request.condominioId(), request.perfil())
                .orElseThrow(() -> ApiException.proibido("Você não possui o vínculo (condomínio, perfil) solicitado"));

        // troca de contexto = encerra a sessão atual e abre outra
        Sessao atual = buscarSessao(usuario);
        sessaoRepository.delete(atual);
        Sessao nova = criarSessao(atual.getPessoa(), vinculo.getPerfil(), vinculo.getCondominio(), vinculo.getUnidade());
        return responderContexto(nova);
    }

    @Transactional
    public ContextoAtivoResponse renovarToken(RefreshRequest request) {
        Sessao atual = sessaoRepository.findByRefreshToken(request.refreshToken())
                .filter(sessao -> sessao.getRefreshExpiraEm().isAfter(Instant.now()))
                .orElseThrow(() -> ApiException.naoAutenticado("Refresh token inválido ou expirado"));

        // rotação: o refresh token usado deixa de valer
        sessaoRepository.delete(atual);
        Sessao nova = criarSessao(atual.getPessoa(), atual.getPerfil(), atual.getCondominio(), atual.getUnidade());
        return responderContexto(nova);
    }

    @Transactional
    public void logout(UsuarioLogado usuario) {
        sessaoRepository.deleteById(usuario.sessaoId());
    }

    private Sessao criarSessao(Pessoa pessoa, Perfil perfil, Condominio condominio, Unidade unidade) {
        Sessao sessao = new Sessao();
        sessao.setId(UUID.randomUUID().toString());
        sessao.setPessoa(pessoa);
        sessao.setPerfil(perfil);
        sessao.setCondominio(condominio);
        sessao.setUnidade(unidade);
        sessao.setRefreshToken(UUID.randomUUID().toString());
        sessao.setRefreshExpiraEm(Instant.now().plus(VALIDADE_REFRESH));
        return sessaoRepository.save(sessao);
    }

    private Sessao buscarSessao(UsuarioLogado usuario) {
        return sessaoRepository.findById(usuario.sessaoId())
                .orElseThrow(() -> ApiException.naoAutenticado("Sessão encerrada"));
    }

    private ContextoAtivoResponse responderContexto(Sessao sessao) {
        return new ContextoAtivoResponse(tokenService.gerarAccessToken(sessao), sessao.getRefreshToken(), TIPO_TOKEN,
                TokenService.VALIDADE_SEGUNDOS, ContextoAtivo.de(sessao));
    }
}
