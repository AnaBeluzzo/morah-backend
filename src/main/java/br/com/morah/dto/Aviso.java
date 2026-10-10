package br.com.morah.dto;

import java.time.Instant;

public record Aviso(
        Long id,
        String titulo,
        String conteudo,
        String categoria,
        PrioridadeAviso prioridade,
        PublicoAlvo publicoAlvo,
        PessoaResumo autor,
        Instant publicadoEm,
        Instant expiraEm,
        boolean lidoPeloUsuario) {

    public static Aviso de(br.com.morah.model.Aviso aviso, boolean lido) {
        return new Aviso(aviso.getId(), aviso.getTitulo(), aviso.getConteudo(), aviso.getCategoria(),
                aviso.getPrioridade(), PublicoAlvo.de(aviso.getPublicoAlvo()), PessoaResumo.de(aviso.getAutor()),
                aviso.getPublicadoEm(), aviso.getExpiraEm(), lido);
    }
}
