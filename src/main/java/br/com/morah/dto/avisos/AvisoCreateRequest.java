package br.com.morah.dto.avisos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AvisoCreateRequest(
        @NotBlank String titulo,
        @NotBlank String conteudo,
        String categoria,
        PrioridadeAviso prioridade,
        @NotNull @Valid PublicoAlvo publicoAlvo,
        Instant expiraEm) {
}
