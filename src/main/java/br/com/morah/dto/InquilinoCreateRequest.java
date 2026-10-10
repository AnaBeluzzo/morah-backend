package br.com.morah.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InquilinoCreateRequest(
        @NotBlank String nome,
        @NotBlank String cpf,
        @Email String email,
        String telefone,
        @NotNull LocalDate contratoInicio,
        LocalDate contratoFim) {
}
