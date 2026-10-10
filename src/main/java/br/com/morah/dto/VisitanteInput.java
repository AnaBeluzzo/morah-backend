package br.com.morah.dto;

import jakarta.validation.constraints.NotBlank;

public record VisitanteInput(
        @NotBlank String nome,
        String documento,
        String telefone) {
}
