package br.com.morah.dto.minhaunidade;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH: só os campos enviados são alterados. */
public record VeiculoUpdateRequest(
        @Pattern(regexp = VeiculoCreateRequest.PADRAO_PLACA, message = VeiculoCreateRequest.MENSAGEM_PLACA) String placa,
        @Size(min = 1, max = 60) String modelo,
        @Size(max = 30) String cor,
        TipoVeiculo tipo) {
}
