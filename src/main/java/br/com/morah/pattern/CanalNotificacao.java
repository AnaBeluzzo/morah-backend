package br.com.morah.pattern;

import br.com.morah.model.Notificacao;
import br.com.morah.model.Pessoa;

/**
 * Strategy.
 * Problema que resolve: cada notificação sai por um canal diferente (push ou e-mail)
 * sem que o NotificacaoTemplate precise de if/else para cada forma de envio.
 */
public interface CanalNotificacao {

    void enviar(Pessoa destinatario, Notificacao notificacao);
}
