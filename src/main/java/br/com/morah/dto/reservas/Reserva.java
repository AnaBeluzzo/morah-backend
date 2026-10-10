package br.com.morah.dto.reservas;

import br.com.morah.dto.comum.PessoaResumo;

import java.math.BigDecimal;
import java.time.Instant;

public record Reserva(
        Long id,
        Long areaComumId,
        String areaComumNome,
        Long unidadeId,
        PessoaResumo solicitante,
        Instant inicio,
        Instant fim,
        StatusReserva status,
        BigDecimal valor) {

    public static Reserva de(br.com.morah.model.reservas.Reserva reserva) {
        return new Reserva(reserva.getId(), reserva.getAreaComum().getId(), reserva.getAreaComum().getNome(),
                reserva.getUnidade().getId(), PessoaResumo.de(reserva.getSolicitante()), reserva.getInicio(),
                reserva.getFim(), reserva.getStatus(), reserva.getValor());
    }
}
