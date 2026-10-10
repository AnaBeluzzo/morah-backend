package br.com.morah.pattern.strategy.canal;

import br.com.morah.model.login.Pessoa;
import br.com.morah.model.notificacoes.Notificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Estratégia de envio por e-mail (simulado com log). Usada pelo financeiro e pelo convite de inquilino. */
@Component
public class CanalEmail implements CanalNotificacao {

    private static final Logger log = LoggerFactory.getLogger(CanalEmail.class);

    @Override
    public void enviar(Pessoa destinatario, Notificacao notificacao) {
        log.info("[E-MAIL] para {}: {} - {}", destinatario.getEmail(), notificacao.getTitulo(), notificacao.getMensagem());
    }
}
