package br.com.morah.pattern.templatemethod.notificacao;

import br.com.morah.dto.financeiro.StatusCobranca;
import br.com.morah.dto.notificacoes.TipoNotificacao;
import br.com.morah.model.financeiro.Cobranca;
import br.com.morah.model.notificacoes.Notificacao;
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
