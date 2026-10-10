package br.com.morah.dto;

import java.math.BigDecimal;

public record Taxa(
        Long id,
        String descricao,
        String competencia,
        BigDecimal valor,
        TipoRateio tipoRateio,
        Integer vencimentoDia) {

    public static Taxa de(br.com.morah.model.Taxa taxa) {
        return new Taxa(taxa.getId(), taxa.getDescricao(), taxa.getCompetencia(), taxa.getValor(),
                taxa.getTipoRateio(), taxa.getVencimentoDia());
    }
}
