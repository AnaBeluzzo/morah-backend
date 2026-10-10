package br.com.morah.dto.reservas;

import java.time.LocalDate;
import java.util.List;

public record Disponibilidade(LocalDate data, List<HorarioDisponivel> horarios) {
}
