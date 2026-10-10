package br.com.morah.dto;

import java.math.BigDecimal;

public record AreaComum(
        Long id,
        String nome,
        String descricao,
        Integer capacidade,
        BigDecimal taxa,
        boolean requerReserva,
        String status) {

    public static AreaComum de(br.com.morah.model.AreaComum area) {
        return new AreaComum(area.getId(), area.getNome(), area.getDescricao(), area.getCapacidade(), area.getTaxa(),
                area.isRequerReserva(), area.getStatus());
    }
}
