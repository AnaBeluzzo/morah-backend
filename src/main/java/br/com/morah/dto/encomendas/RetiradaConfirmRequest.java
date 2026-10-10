package br.com.morah.dto.encomendas;

import jakarta.validation.constraints.NotBlank;

public record RetiradaConfirmRequest(
        @NotBlank String codigo,
        String retiradoPorNome,
        String retiradoPorDocumento) {
}
