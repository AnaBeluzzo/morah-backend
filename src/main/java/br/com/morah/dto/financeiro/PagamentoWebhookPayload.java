package br.com.morah.dto.financeiro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record PagamentoWebhookPayload(
        @NotBlank String transacaoId,
        @NotNull Long cobrancaId,
        BigDecimal valor,
        @NotNull StatusPagamentoWebhook status,
        Instant pagoEm) {
}
