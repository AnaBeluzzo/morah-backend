package br.com.morah.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EncomendaCreateRequest(
        @NotNull Long unidadeId,
        String remetente,
        @NotBlank String transportadora,
        String codigoRastreio,
        String fotoUrl) {
}
