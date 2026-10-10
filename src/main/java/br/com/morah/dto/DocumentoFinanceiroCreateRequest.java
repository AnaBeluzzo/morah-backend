package br.com.morah.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record DocumentoFinanceiroCreateRequest(
        @NotBlank String tipo,
        @NotBlank String titulo,
        @NotBlank String arquivoUrl,
        LocalDate competencia) {
}
