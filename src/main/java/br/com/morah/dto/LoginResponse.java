package br.com.morah.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        ContextoAtivo contexto,
        List<PerfilDisponivel> perfisDisponiveis) {
}
