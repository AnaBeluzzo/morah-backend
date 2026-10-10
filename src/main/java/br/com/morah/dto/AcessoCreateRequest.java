package br.com.morah.dto;

import jakarta.validation.constraints.NotNull;

public record AcessoCreateRequest(
        Long autorizacaoId,
        @NotNull Long visitanteId,
        @NotNull TipoAcesso tipo,
        String fotoUrl) {
}
