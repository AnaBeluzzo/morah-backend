package br.com.morah.dto.avisos;

import java.time.Instant;

/** Todos os campos opcionais: o PATCH só altera o que vier preenchido. */
public record AvisoUpdateRequest(
        String titulo,
        String conteudo,
        String categoria,
        PrioridadeAviso prioridade,
        Instant expiraEm) {
}
