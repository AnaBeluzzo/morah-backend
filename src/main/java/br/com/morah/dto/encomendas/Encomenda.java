package br.com.morah.dto.encomendas;

import java.time.Instant;

public record Encomenda(
        Long id,
        Long unidadeId,
        String remetente,
        String transportadora,
        String codigoRastreio,
        String codigoRetirada,
        String fotoUrl,
        Instant recebidaEm,
        Instant retiradaEm,
        StatusEncomenda status) {

    public static Encomenda de(br.com.morah.model.encomendas.Encomenda encomenda) {
        return new Encomenda(encomenda.getId(), encomenda.getUnidade().getId(), encomenda.getRemetente(),
                encomenda.getTransportadora(), encomenda.getCodigoRastreio(), encomenda.getCodigoRetirada(),
                encomenda.getFotoUrl(), encomenda.getRecebidaEm(), encomenda.getRetiradaEm(), encomenda.getStatus());
    }
}
