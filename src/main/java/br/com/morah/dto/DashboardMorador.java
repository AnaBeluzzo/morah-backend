package br.com.morah.dto;

import java.util.List;

/** Usado por morador e proprietário. */
public record DashboardMorador(
        Perfil perfil,
        Unidade unidade,
        List<Cobranca> proximosBoletos,
        List<Aviso> avisosRecentes,
        List<Encomenda> encomendasPendentes,
        List<AutorizacaoVisita> autorizacoesPendentes,
        List<Reserva> proximasReservas) implements Dashboard {
}
