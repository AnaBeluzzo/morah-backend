package br.com.morah.dto.reservas;

import java.time.Instant;

public record HorarioDisponivel(Instant inicio, Instant fim, boolean disponivel) {
}
