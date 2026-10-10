package br.com.morah.dto.login;

import jakarta.validation.constraints.NotNull;

public record ContextoRequest(
        @NotNull Long condominioId,
        @NotNull Perfil perfil) {
}
