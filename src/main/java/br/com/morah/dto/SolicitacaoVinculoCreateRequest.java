package br.com.morah.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitacaoVinculoCreateRequest(
        @NotNull TipoVinculo tipoVinculo,
        @NotBlank String nome,
        String cpf,
        String observacao) {
}
