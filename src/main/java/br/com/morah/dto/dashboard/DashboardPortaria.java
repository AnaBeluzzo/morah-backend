package br.com.morah.dto.dashboard;

import br.com.morah.dto.login.Perfil;
import br.com.morah.dto.portaria.AutorizacaoVisita;
import br.com.morah.dto.reservas.Reserva;

import java.util.List;

public record DashboardPortaria(
        Perfil perfil,
        List<AutorizacaoVisita> autorizacoesPendentes,
        List<Reserva> reservasDoDia,
        long encomendasRecebidasHoje) implements Dashboard {
}
