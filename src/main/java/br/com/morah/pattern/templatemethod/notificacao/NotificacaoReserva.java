package br.com.morah.pattern.templatemethod.notificacao;

import br.com.morah.dto.notificacoes.TipoNotificacao;
import br.com.morah.model.notificacoes.Notificacao;
import br.com.morah.model.reservas.Reserva;
import org.springframework.stereotype.Component;

/** Passo variável do NotificacaoTemplate para uma reserva confirmada. */
@Component
public class NotificacaoReserva extends NotificacaoTemplate<Reserva> {

    @Override
    protected Notificacao montarPayload(Reserva reserva) {
        return payload(TipoNotificacao.RESERVA, "Reserva confirmada",
                reserva.getAreaComum().getNome() + " reservado(a) a partir de " + reserva.getInicio() + ".",
                reserva.getId());
    }
}
