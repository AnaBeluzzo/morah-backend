package br.com.morah.pattern;

import br.com.morah.dto.PagamentoWebhookPayload;
import br.com.morah.dto.StatusPagamentoWebhook;
import br.com.morah.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Origem "webhook do gateway": autenticada pela assinatura HMAC-SHA256 do corpo bruto, não por JWT. */
@Component
public class WebhookPagamentoProcessador extends ProcessamentoPagamentoTemplate<HttpEntity<String>> {

    public static final String CABECALHO_ASSINATURA = "X-Webhook-Signature";

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final String segredo;

    public WebhookPagamentoProcessador(ObjectMapper objectMapper, Validator validator,
                                       @Value("${morah.webhook.segredo}") String segredo) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.segredo = segredo;
    }

    @Override
    protected Long validarOrigem(HttpEntity<String> requisicao) {
        String corpo = requisicao.getBody();
        String assinatura = requisicao.getHeaders().getFirst(CABECALHO_ASSINATURA);
        // a assinatura é conferida sobre o corpo exatamente como chegou
        if (corpo == null || assinatura == null || !MessageDigest.isEqual(
                assinar(corpo, segredo).getBytes(StandardCharsets.UTF_8),
                assinatura.toLowerCase().getBytes(StandardCharsets.UTF_8))) {
            throw ApiException.naoAutenticado("Assinatura do webhook ausente ou inválida");
        }

        PagamentoWebhookPayload payload = lerPayload(corpo);
        // "falhou" encerra o fluxo sem alterar nada
        return payload.status() == StatusPagamentoWebhook.CONFIRMADO ? payload.cobrancaId() : null;
    }

    /** HMAC-SHA256 em hexadecimal (usado também pelos testes para assinar o corpo). */
    public static String assinar(String corpo, String segredo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível calcular o HMAC", e);
        }
    }

    private PagamentoWebhookPayload lerPayload(String corpo) {
        try {
            PagamentoWebhookPayload payload = objectMapper.readValue(corpo, PagamentoWebhookPayload.class);
            if (!validator.validate(payload).isEmpty()) {
                throw ApiException.requisicaoInvalida("transacaoId, cobrancaId e status são obrigatórios");
            }
            return payload;
        } catch (JsonProcessingException e) {
            throw ApiException.requisicaoInvalida("Payload do webhook mal formatado");
        }
    }
}
