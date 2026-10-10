package br.com.morah.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AutorizacaoVisitaCreateRequest(
        @NotNull Long unidadeId,
        @NotNull @Valid VisitanteInput visitante,
        @NotBlank String motivo,
        String fotoUrl) {
}
