package br.com.morah.dto;

import java.time.Instant;

public record HorarioDisponivel(Instant inicio, Instant fim, boolean disponivel) {
}
