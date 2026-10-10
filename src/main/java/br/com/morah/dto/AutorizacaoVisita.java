package br.com.morah.dto;

import java.time.Instant;

public record AutorizacaoVisita(
        Long id,
        Visitante visitante,
        Long unidadeId,
        String motivo,
        Instant solicitadoEm,
        Instant respondidoEm,
        StatusAutorizacaoVisita status) {

    public static AutorizacaoVisita de(br.com.morah.model.AutorizacaoVisita autorizacao) {
        return new AutorizacaoVisita(autorizacao.getId(), Visitante.de(autorizacao.getVisitante()),
                autorizacao.getUnidade().getId(), autorizacao.getMotivo(), autorizacao.getSolicitadoEm(),
                autorizacao.getRespondidoEm(), autorizacao.calcularStatus());
    }
}
