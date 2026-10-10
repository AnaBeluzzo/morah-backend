package br.com.morah.pattern;

import br.com.morah.dto.TipoNotificacao;
import br.com.morah.model.AutorizacaoVisita;
import br.com.morah.model.Notificacao;
import org.springframework.stereotype.Component;

/** Passo variável do NotificacaoTemplate para um visitante aguardando autorização. */
@Component
public class NotificacaoVisitante extends NotificacaoTemplate<AutorizacaoVisita> {

    @Override
    protected Notificacao montarPayload(AutorizacaoVisita autorizacao) {
        return payload(TipoNotificacao.PORTARIA, "Visitante na portaria",
                autorizacao.getVisitante().getNome() + " aguarda autorização de entrada: " + autorizacao.getMotivo(),
                autorizacao.getId());
    }
}
