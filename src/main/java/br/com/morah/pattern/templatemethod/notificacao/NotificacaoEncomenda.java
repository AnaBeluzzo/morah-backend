package br.com.morah.pattern.templatemethod.notificacao;

import br.com.morah.dto.notificacoes.TipoNotificacao;
import br.com.morah.model.encomendas.Encomenda;
import br.com.morah.model.notificacoes.Notificacao;
import org.springframework.stereotype.Component;

/** Passo variável do NotificacaoTemplate para uma encomenda recebida na portaria. */
@Component
public class NotificacaoEncomenda extends NotificacaoTemplate<Encomenda> {

    @Override
    protected Notificacao montarPayload(Encomenda encomenda) {
        return payload(TipoNotificacao.ENCOMENDA, "Encomenda na portaria",
                "Chegou uma encomenda (" + encomenda.getTransportadora() + "). Código de retirada: "
                        + encomenda.getCodigoRetirada() + ".",
                encomenda.getId());
    }
}
