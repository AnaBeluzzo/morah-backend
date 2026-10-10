package br.com.morah.pattern.templatemethod.notificacao;

import br.com.morah.dto.notificacoes.TipoNotificacao;
import br.com.morah.model.notificacoes.Notificacao;
import br.com.morah.model.portaria.AutorizacaoVisita;
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
