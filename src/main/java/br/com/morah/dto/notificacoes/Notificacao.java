package br.com.morah.dto.notificacoes;

import java.time.Instant;

public record Notificacao(
        Long id,
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        Long referenciaId,
        Instant enviadaEm,
        Instant lidaEm) {

    public static Notificacao de(br.com.morah.model.notificacoes.Notificacao notificacao) {
        return new Notificacao(notificacao.getId(), notificacao.getTipo(), notificacao.getTitulo(),
                notificacao.getMensagem(), notificacao.getReferenciaId(), notificacao.getEnviadaEm(),
                notificacao.getLidaEm());
    }
}
