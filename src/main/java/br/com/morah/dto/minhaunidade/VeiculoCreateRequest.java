package br.com.morah.dto.minhaunidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VeiculoCreateRequest(
        @NotBlank @Pattern(regexp = VeiculoCreateRequest.PADRAO_PLACA, message = VeiculoCreateRequest.MENSAGEM_PLACA) String placa,
        @NotBlank @Size(max = 60) String modelo,
        @Size(max = 30) String cor,
        @NotNull TipoVeiculo tipo) {

    /** Placa antiga (ABC1234) ou Mercosul (ABC1D23), com ou sem hífen, maiúsculas ou minúsculas. */
    public static final String PADRAO_PLACA = "(?i)[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}";
    public static final String MENSAGEM_PLACA = "deve estar no formato ABC1234 ou ABC1D23";
}
