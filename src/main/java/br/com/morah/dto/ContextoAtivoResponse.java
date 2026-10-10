package br.com.morah.dto;

public record ContextoAtivoResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        ContextoAtivo contexto) {
}
