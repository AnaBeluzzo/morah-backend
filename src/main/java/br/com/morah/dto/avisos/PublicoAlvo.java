package br.com.morah.dto.avisos;

import jakarta.validation.constraints.NotNull;

public record PublicoAlvo(
        @NotNull TipoPublicoAlvo tipo,
        Long blocoId,
        Long unidadeId) {

    public static PublicoAlvo de(br.com.morah.model.avisos.PublicoAlvo publico) {
        return new PublicoAlvo(publico.getTipo(), publico.getBlocoId(), publico.getUnidadeId());
    }
}
