package br.com.morah.pattern;

import br.com.morah.dto.StatusCobranca;
import br.com.morah.dto.TipoNotificacao;
import br.com.morah.model.Cobranca;
import br.com.morah.model.Notificacao;
import org.springframework.stereotype.Component;

/** Passo variável do NotificacaoTemplate para cobranças: nova cobrança ou pagamento confirmado. */
@Component
public class NotificacaoFinanceiro extends NotificacaoTemplate<Cobranca> {

    @Override
    protected Notificacao montarPayload(Cobranca cobranca) {
        if (cobranca.getStatus() == StatusCobranca.PAGO) {
            return payload(TipoNotificacao.FINANCEIRO, "Pagamento confirmado",
                    "Recebemos o pagamento de " + cobranca.getTaxa().getDescricao() + " (" + cobranca.getCompetencia() + ").",
                    cobranca.getId());
        }
        return payload(TipoNotificacao.FINANCEIRO, "Nova cobrança",
                cobranca.getTaxa().getDescricao() + " (" + cobranca.getCompetencia() + ") vence em " + cobranca.getVencimento() + ".",
                cobranca.getId());
    }
}
