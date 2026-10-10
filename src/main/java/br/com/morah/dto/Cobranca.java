package br.com.morah.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Cobranca(
        Long id,
        Long unidadeId,
        String taxaDescricao,
        String competencia,
        LocalDate vencimento,
        BigDecimal valorOriginal,
        BigDecimal multa,
        BigDecimal juros,
        BigDecimal valorTotal,
        StatusCobranca status) {

    public static Cobranca de(br.com.morah.model.Cobranca cobranca) {
        return new Cobranca(cobranca.getId(), cobranca.getUnidade().getId(), cobranca.getTaxa().getDescricao(),
                cobranca.getCompetencia(), cobranca.getVencimento(), cobranca.getValorOriginal(), cobranca.getMulta(),
                cobranca.getJuros(), cobranca.calcularValorTotal(), cobranca.calcularStatus());
    }
}
