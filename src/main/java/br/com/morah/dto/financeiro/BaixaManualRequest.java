package br.com.morah.dto.financeiro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record BaixaManualRequest(
        @NotBlank String meio,
        @NotNull BigDecimal valor,
        @NotNull Instant pagoEm,
        String observacao) {
}
