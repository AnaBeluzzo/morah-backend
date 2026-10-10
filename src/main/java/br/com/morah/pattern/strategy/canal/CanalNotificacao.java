package br.com.morah.pattern.strategy.canal;

import br.com.morah.model.login.Pessoa;
import br.com.morah.model.notificacoes.Notificacao;

/**
 * Strategy.
 * Problema que resolve: cada notificação sai por um canal diferente (push ou e-mail)
 * sem que o NotificacaoTemplate precise de if/else para cada forma de envio.
 */
public interface CanalNotificacao {

    void enviar(Pessoa destinatario, Notificacao notificacao);
}
