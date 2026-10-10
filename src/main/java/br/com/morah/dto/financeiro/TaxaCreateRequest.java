package br.com.morah.dto.financeiro;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TaxaCreateRequest(
        @NotBlank String descricao,
        @NotNull @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "deve estar no formato AAAA-MM") String competencia,
        @NotNull BigDecimal valor,
        @NotNull TipoRateio tipoRateio,
        @NotNull @Min(1) @Max(28) Integer vencimentoDia) {
}
