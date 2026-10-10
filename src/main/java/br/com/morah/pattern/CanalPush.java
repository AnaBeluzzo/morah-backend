package br.com.morah.pattern;

import br.com.morah.model.Notificacao;
import br.com.morah.model.Pessoa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Estratégia de envio por push no app (simulado com log). Usada por portaria, encomenda, aviso e reserva. */
@Component
public class CanalPush implements CanalNotificacao {

    private static final Logger log = LoggerFactory.getLogger(CanalPush.class);

    @Override
    public void enviar(Pessoa destinatario, Notificacao notificacao) {
        log.info("[PUSH] para pessoa {}: {} - {}", destinatario.getId(), notificacao.getTitulo(), notificacao.getMensagem());
    }
}
