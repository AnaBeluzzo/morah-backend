package br.com.morah.dto.login;

public record ContextoAtivoResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        ContextoAtivo contexto) {
}
