package br.com.morah.dto.encomendas;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AutorizacaoRetiradaCreateRequest(
        @NotBlank String nomeTerceiro,
        @NotBlank String documento,
        @Min(1) Integer validadeHoras) {
}
