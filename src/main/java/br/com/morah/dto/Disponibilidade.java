package br.com.morah.dto;

import java.time.LocalDate;
import java.util.List;

public record Disponibilidade(LocalDate data, List<HorarioDisponivel> horarios) {
}
