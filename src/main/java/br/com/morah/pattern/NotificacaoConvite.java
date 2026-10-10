package br.com.morah.pattern;

import br.com.morah.model.Notificacao;
import br.com.morah.model.VinculoUnidade;
import org.springframework.stereotype.Component;

/**
 * Passo variável do NotificacaoTemplate para o convite de acesso do inquilino.
 * O TipoNotificacao do contrato não tem "convite", então o tipo fica vazio.
 */
@Component
public class NotificacaoConvite extends NotificacaoTemplate<VinculoUnidade> {

    @Override
    protected Notificacao montarPayload(VinculoUnidade vinculo) {
        return payload(null, "Convite de acesso ao Morah",
                "Você foi cadastrado como inquilino da unidade " + vinculo.getUnidade().getIdentificacao()
                        + ". Acesse o app para criar sua senha.",
                vinculo.getId());
    }

    /** O convite sempre sai por e-mail: o inquilino ainda não usa o app. */
    @Override
    protected CanalNotificacao escolherCanal(Notificacao payload) {
        return canalEmail;
    }
}
