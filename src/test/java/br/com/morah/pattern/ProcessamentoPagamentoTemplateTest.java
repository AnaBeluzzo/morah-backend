package br.com.morah.pattern;

import br.com.morah.dto.StatusCobranca;
import br.com.morah.exception.ApiException;
import br.com.morah.model.Cobranca;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessamentoPagamentoTemplateTest {

    private static final String SEGREDO = "segredo-de-teste";

    private final List<String> passos = new ArrayList<>();
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    /** Webhook real, com os passos comuns trocados por registros (sem banco). */
    private class WebhookDeTeste extends WebhookPagamentoProcessador {

        WebhookDeTeste() {
            super(objectMapper, Validation.buildDefaultValidatorFactory().getValidator(), SEGREDO);
        }

        @Override
        protected Cobranca localizarCobranca(Long cobrancaId) {
            passos.add("localizar:" + cobrancaId);
            Cobranca cobranca = new Cobranca();
            cobranca.setStatus(StatusCobranca.PENDENTE);
            return cobranca;
        }

        @Override
        protected void verificarSePodeSerPaga(Cobranca cobranca) {
            passos.add("verificar");
        }

        @Override
        protected void marcarComoPago(Cobranca cobranca) {
            passos.add("marcarComoPago");
        }

        @Override
        protected void notificarUnidade(Cobranca cobranca) {
            passos.add("notificar");
        }
    }

    private HttpEntity<String> requisicaoAssinada(String corpo, String assinatura) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.set(WebhookPagamentoProcessador.CABECALHO_ASSINATURA, assinatura);
        return new HttpEntity<>(corpo, cabecalhos);
    }

    @Test
    void deveExecutarOsPassosNaOrdemFixa() {
        String corpo = "{\"transacaoId\":\"t1\",\"cobrancaId\":7,\"status\":\"confirmado\"}";

        new WebhookDeTeste().processar(requisicaoAssinada(corpo, WebhookPagamentoProcessador.assinar(corpo, SEGREDO)));

        assertThat(passos).containsExactly("localizar:7", "verificar", "marcarComoPago", "notificar");
    }

    @Test
    void devePararAntesDeMarcarComoPagoQuandoWebhookFalhou() {
        String corpo = "{\"transacaoId\":\"t1\",\"cobrancaId\":7,\"status\":\"falhou\"}";

        Cobranca resultado = new WebhookDeTeste()
                .processar(requisicaoAssinada(corpo, WebhookPagamentoProcessador.assinar(corpo, SEGREDO)));

        assertThat(resultado).isNull();
        assertThat(passos).isEmpty();
    }

    @Test
    void deveRecusarAssinaturaInvalidaAntesDeQualquerPasso() {
        String corpo = "{\"transacaoId\":\"t1\",\"cobrancaId\":7,\"status\":\"confirmado\"}";

        assertThatThrownBy(() -> new WebhookDeTeste().processar(requisicaoAssinada(corpo, "invalida")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Assinatura");
        assertThat(passos).isEmpty();
    }

    @Test
    void deveRecusarCobrancaJaPagaNoPassoComumDeVerificacao() {
        ProcessamentoPagamentoTemplate<Long> template = new ProcessamentoPagamentoTemplate<>() {
            @Override
            protected Long validarOrigem(Long cobrancaId) {
                return cobrancaId;
            }

            @Override
            protected Cobranca localizarCobranca(Long cobrancaId) {
                Cobranca cobranca = new Cobranca();
                cobranca.setStatus(StatusCobranca.PAGO);
                return cobranca;
            }
        };

        assertThatThrownBy(() -> template.processar(1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("paga ou cancelada");
    }
}
