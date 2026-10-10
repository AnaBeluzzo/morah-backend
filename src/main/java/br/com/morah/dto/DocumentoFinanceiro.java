package br.com.morah.dto;

import java.time.Instant;
import java.time.LocalDate;

public record DocumentoFinanceiro(
        Long id,
        String tipo,
        String titulo,
        String arquivoUrl,
        LocalDate competencia,
        Instant publicadoEm) {

    public static DocumentoFinanceiro de(br.com.morah.model.DocumentoFinanceiro documento) {
        return new DocumentoFinanceiro(documento.getId(), documento.getTipo(), documento.getTitulo(),
                documento.getArquivoUrl(), documento.getCompetencia(), documento.getPublicadoEm());
    }
}
