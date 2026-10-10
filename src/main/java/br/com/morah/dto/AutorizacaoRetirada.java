package br.com.morah.dto;

import java.time.Instant;

public record AutorizacaoRetirada(
        Long id,
        Long encomendaId,
        String nomeTerceiro,
        String documento,
        String codigo,
        Instant validade,
        StatusAutorizacaoRetirada status) {

    public static AutorizacaoRetirada de(br.com.morah.model.AutorizacaoRetirada autorizacao) {
        return new AutorizacaoRetirada(autorizacao.getId(), autorizacao.getEncomenda().getId(),
                autorizacao.getNomeTerceiro(), autorizacao.getDocumento(), autorizacao.getCodigo(),
                autorizacao.getValidade(), autorizacao.calcularStatus());
    }
}
