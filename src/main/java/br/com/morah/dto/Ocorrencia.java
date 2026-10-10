package br.com.morah.dto;

import java.time.Instant;

public record Ocorrencia(
        Long id,
        String titulo,
        String descricao,
        StatusOcorrencia status,
        Instant abertaEm,
        Instant encerradaEm) {

    public static Ocorrencia de(br.com.morah.model.Ocorrencia ocorrencia) {
        return new Ocorrencia(ocorrencia.getId(), ocorrencia.getTitulo(), ocorrencia.getDescricao(),
                ocorrencia.getStatus(), ocorrencia.getAbertaEm(), ocorrencia.getEncerradaEm());
    }
}
