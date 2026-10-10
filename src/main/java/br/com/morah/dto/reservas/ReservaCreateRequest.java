package br.com.morah.dto.reservas;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReservaCreateRequest(
        @NotNull Long areaComumId,
        @NotNull Instant inicio,
        @NotNull Instant fim) {
}
