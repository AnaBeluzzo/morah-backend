package br.com.morah.dto.dashboard;

import br.com.morah.dto.avisos.Aviso;
import br.com.morah.dto.encomendas.Encomenda;
import br.com.morah.dto.financeiro.Cobranca;
import br.com.morah.dto.login.Perfil;
import br.com.morah.dto.minhaunidade.Unidade;
import br.com.morah.dto.portaria.AutorizacaoVisita;
import br.com.morah.dto.reservas.Reserva;

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
