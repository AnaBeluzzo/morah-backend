package br.com.morah.dto;

import jakarta.validation.constraints.NotNull;

public record ModuloUpdateRequest(@NotNull Boolean ativo) {
}
