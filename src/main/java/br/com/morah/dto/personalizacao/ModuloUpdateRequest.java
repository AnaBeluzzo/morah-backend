package br.com.morah.dto.personalizacao;

import jakarta.validation.constraints.NotNull;

public record ModuloUpdateRequest(@NotNull Boolean ativo) {
}
