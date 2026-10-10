package br.com.morah.dto;

import java.util.List;

public record DashboardPortaria(
        Perfil perfil,
        List<AutorizacaoVisita> autorizacoesPendentes,
        List<Reserva> reservasDoDia,
        long encomendasRecebidasHoje) implements Dashboard {
}
