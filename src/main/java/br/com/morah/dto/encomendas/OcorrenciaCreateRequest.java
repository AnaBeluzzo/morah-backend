package br.com.morah.dto.encomendas;

import jakarta.validation.constraints.NotBlank;

public record OcorrenciaCreateRequest(
        @NotBlank String titulo,
        @NotBlank String descricao) {
}
