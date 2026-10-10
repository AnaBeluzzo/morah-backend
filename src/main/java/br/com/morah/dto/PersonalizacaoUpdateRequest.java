package br.com.morah.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Todos os campos opcionais. "logoUrl": null remove a logo (ver CondominioController). */
public record PersonalizacaoUpdateRequest(
        @Size(min = 1, max = 60) String nomeExibicao,
        String logoUrl,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "deve estar no formato #RRGGBB") String corPrimaria,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "deve estar no formato #RRGGBB") String corSecundaria,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "deve estar no formato #RRGGBB") String corDestaque) {
}
