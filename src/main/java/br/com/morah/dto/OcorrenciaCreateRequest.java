package br.com.morah.dto;

import jakarta.validation.constraints.NotBlank;

public record OcorrenciaCreateRequest(
        @NotBlank String titulo,
        @NotBlank String descricao) {
}
