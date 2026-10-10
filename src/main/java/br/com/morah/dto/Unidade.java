package br.com.morah.dto;

import java.math.BigDecimal;

public record Unidade(
        Long id,
        Long blocoId,
        String blocoNome,
        String identificacao,
        String tipo,
        BigDecimal areaM2,
        BigDecimal fracaoIdeal,
        String status) {

    public static Unidade de(br.com.morah.model.Unidade unidade) {
        return new Unidade(unidade.getId(), unidade.getBloco().getId(), unidade.getBloco().getNome(),
                unidade.getIdentificacao(), unidade.getTipo(), unidade.getAreaM2(), unidade.getFracaoIdeal(),
                unidade.getStatus());
    }
}
