package br.com.morah.pattern;

import br.com.morah.dto.TipoNotificacao;
import br.com.morah.model.Aviso;
import br.com.morah.model.Notificacao;
import org.springframework.stereotype.Component;

/** Passo variável do NotificacaoTemplate para um aviso publicado. */
@Component
public class NotificacaoAviso extends NotificacaoTemplate<Aviso> {

    @Override
    protected Notificacao montarPayload(Aviso aviso) {
        return payload(TipoNotificacao.AVISO, "Novo aviso: " + aviso.getTitulo(), aviso.getConteudo(), aviso.getId());
    }
}
