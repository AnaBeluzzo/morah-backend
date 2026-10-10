package br.com.morah.dto.minhaunidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitacaoVinculoCreateRequest(
        @NotNull TipoVinculo tipoVinculo,
        @NotBlank String nome,
        String cpf,
        String observacao) {
}
