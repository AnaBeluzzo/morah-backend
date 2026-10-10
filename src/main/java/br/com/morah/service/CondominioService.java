package br.com.morah.service;

import br.com.morah.dto.ModuloCodigo;
import br.com.morah.dto.PersonalizacaoUpdateRequest;
import br.com.morah.exception.ApiException;
import br.com.morah.model.ModuloCondominio;
import br.com.morah.model.Personalizacao;
import br.com.morah.pattern.CacheModulosAtivos;
import br.com.morah.repository.CondominioRepository;
import br.com.morah.repository.ModuloCondominioRepository;
import br.com.morah.repository.PersonalizacaoRepository;
import br.com.morah.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class CondominioService {

    private final PersonalizacaoRepository personalizacaoRepository;
    private final ModuloCondominioRepository moduloCondominioRepository;
    private final CondominioRepository condominioRepository;

    public CondominioService(PersonalizacaoRepository personalizacaoRepository,
                             ModuloCondominioRepository moduloCondominioRepository,
                             CondominioRepository condominioRepository) {
        this.personalizacaoRepository = personalizacaoRepository;
        this.moduloCondominioRepository = moduloCondominioRepository;
        this.condominioRepository = condominioRepository;
    }

    @Transactional(readOnly = true)
    public br.com.morah.dto.Personalizacao buscarPersonalizacao(UsuarioLogado usuario) {
        return br.com.morah.dto.Personalizacao.de(buscarDoCondominio(usuario));
    }

    /** PATCH: só altera o que foi enviado; logoEnviada + valor null remove a logo. */
    @Transactional
    public br.com.morah.dto.Personalizacao alterarPersonalizacao(UsuarioLogado usuario, PersonalizacaoUpdateRequest request,
                                                                boolean logoEnviada) {
        Personalizacao personalizacao = buscarDoCondominio(usuario);
        if (request.nomeExibicao() != null) {
            personalizacao.setNomeExibicao(request.nomeExibicao());
        }
        if (logoEnviada) {
            personalizacao.setLogoUrl(request.logoUrl());
        }
        if (request.corPrimaria() != null) {
            personalizacao.setCorPrimaria(request.corPrimaria());
        }
        if (request.corSecundaria() != null) {
            personalizacao.setCorSecundaria(request.corSecundaria());
        }
        if (request.corDestaque() != null) {
            personalizacao.setCorDestaque(request.corDestaque());
        }
        personalizacao.setAtualizadoEm(Instant.now());
        return br.com.morah.dto.Personalizacao.de(personalizacao);
    }

    @Transactional(readOnly = true)
    public List<br.com.morah.dto.ModuloCondominio> buscarModulos(UsuarioLogado usuario) {
        return moduloCondominioRepository.findByCondominioIdOrderById(usuario.condominioId()).stream()
                .map(br.com.morah.dto.ModuloCondominio::de)
                .toList();
    }

    @Transactional
    public br.com.morah.dto.ModuloCondominio alterarModulo(UsuarioLogado usuario, ModuloCodigo codigo, boolean ativo) {
        ModuloCondominio modulo = moduloCondominioRepository.findByCondominioIdAndModulo(usuario.condominioId(), codigo)
                .orElseGet(() -> {
                    ModuloCondominio novo = new ModuloCondominio();
                    novo.setCondominio(condominioRepository.getReferenceById(usuario.condominioId()));
                    novo.setModulo(codigo);
                    return novo;
                });
        modulo.setAtivo(ativo);
        modulo.setAtualizadoEm(Instant.now());
        moduloCondominioRepository.save(modulo);

        // o cache em memória deixa de valer para este condomínio
        CacheModulosAtivos.INSTANCE.invalidar(usuario.condominioId());
        return br.com.morah.dto.ModuloCondominio.de(modulo);
    }

    /** Consulta o Singleton de cache; só vai ao banco quando o condomínio ainda não está em memória. */
    public boolean isModuloAtivo(Long condominioId, ModuloCodigo modulo) {
        return CacheModulosAtivos.INSTANCE.buscarAtivos(condominioId, this::carregarModulosAtivos).contains(modulo);
    }

    /** Ativos = todos os módulos menos os marcados como desativados no banco. */
    private Set<ModuloCodigo> carregarModulosAtivos(Long condominioId) {
        Set<ModuloCodigo> ativos = EnumSet.allOf(ModuloCodigo.class);
        moduloCondominioRepository.findByCondominioIdAndAtivoFalse(condominioId)
                .forEach(desativado -> ativos.remove(desativado.getModulo()));
        return ativos;
    }

    private Personalizacao buscarDoCondominio(UsuarioLogado usuario) {
        return personalizacaoRepository.findByCondominioId(usuario.condominioId())
                .orElseThrow(() -> ApiException.naoEncontrado("Personalização não encontrada"));
    }
}
