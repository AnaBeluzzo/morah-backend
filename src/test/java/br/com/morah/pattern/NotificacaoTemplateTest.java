package br.com.morah.pattern;

import br.com.morah.dto.TipoNotificacao;
import br.com.morah.model.Notificacao;
import br.com.morah.model.Pessoa;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoTemplateTest {

    private final List<String> passos = new ArrayList<>();

    /** Template de teste que só registra a ordem dos passos. */
    private class NotificacaoDeTeste extends NotificacaoTemplate<String> {

        @Override
        protected Notificacao montarPayload(String evento) {
            passos.add("montar");
            return payload(TipoNotificacao.AVISO, evento, "mensagem", 1L);
        }

        @Override
        protected CanalNotificacao escolherCanal(Notificacao payload) {
            passos.add("escolher");
            return (destinatario, notificacao) -> passos.add("enviar");
        }

        @Override
        protected void registrar(Pessoa destinatario, Notificacao payload) {
            passos.add("registrar");
        }
    }

    /** Template que usa a escolha de canal padrão (para testar a Strategy). */
    private static class NotificacaoPadrao extends NotificacaoTemplate<TipoNotificacao> {
        @Override
        protected Notificacao montarPayload(TipoNotificacao tipo) {
            return payload(tipo, "titulo", "mensagem", 1L);
        }
    }

    @Test
    void deveExecutarOsPassosNaOrdemFixa() {
        new NotificacaoDeTeste().notificar("aviso", List.of(new Pessoa()));

        assertThat(passos).containsExactly("montar", "escolher", "enviar", "registrar");
    }

    @Test
    void deveEnviarERegistrarUmaVezPorDestinatario() {
        new NotificacaoDeTeste().notificar("aviso", List.of(new Pessoa(), new Pessoa()));

        assertThat(passos).containsExactly("montar", "escolher", "enviar", "registrar", "enviar", "registrar");
    }

    @Test
    void deveTrocarOCanalConformeOTipoDaNotificacao() {
        NotificacaoPadrao template = new NotificacaoPadrao();
        template.canalPush = new CanalPush();
        template.canalEmail = new CanalEmail();

        assertThat(template.escolherCanal(template.montarPayload(TipoNotificacao.FINANCEIRO))).isInstanceOf(CanalEmail.class);
        assertThat(template.escolherCanal(template.montarPayload(TipoNotificacao.ENCOMENDA))).isInstanceOf(CanalPush.class);
    }
}
