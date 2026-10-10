package br.com.morah.security;

import br.com.morah.dto.Perfil;
import br.com.morah.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Contexto da requisição lido das claims do JWT.
 * Condomínio, unidade e perfil vêm SEMPRE daqui, nunca da URL.
 */
public record UsuarioLogado(Long pessoaId, String sessaoId, Perfil perfil, Long condominioId, Long unidadeId) {

    public static UsuarioLogado atual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (!(autenticacao instanceof JwtAuthenticationToken token)) {
            throw ApiException.naoAutenticado("Token de acesso ausente");
        }
        return de(token.getToken());
    }

    public static UsuarioLogado de(Jwt jwt) {
        return new UsuarioLogado(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString("sid"),
                Perfil.de(jwt.getClaimAsString("perfil")),
                comoLong(jwt.getClaim("condominio_id")),
                comoLong(jwt.getClaim("unidade_id")));
    }

    /** Morador e proprietário só enxergam a própria unidade. */
    public boolean isMoradorOuProprietario() {
        return perfil == Perfil.MORADOR || perfil == Perfil.PROPRIETARIO;
    }

    private static Long comoLong(Object valor) {
        return valor == null ? null : ((Number) valor).longValue();
    }
}
