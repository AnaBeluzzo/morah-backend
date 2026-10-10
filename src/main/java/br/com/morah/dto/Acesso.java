package br.com.morah.dto;

import java.time.Instant;

public record Acesso(
        Long id,
        Long autorizacaoId,
        Visitante visitante,
        Instant entrada,
        Instant saida,
        TipoAcesso tipo,
        StatusAcesso status) {

    public static Acesso de(br.com.morah.model.Acesso acesso) {
        return new Acesso(acesso.getId(),
                acesso.getAutorizacao() == null ? null : acesso.getAutorizacao().getId(),
                Visitante.de(acesso.getVisitante()), acesso.getEntrada(), acesso.getSaida(), acesso.getTipo(),
                acesso.getStatus());
    }
}
