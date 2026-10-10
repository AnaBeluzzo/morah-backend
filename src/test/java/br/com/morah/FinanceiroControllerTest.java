package br.com.morah;

import br.com.morah.pattern.WebhookPagamentoProcessador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FinanceiroControllerTest extends IntegracaoTest {

    @Value("${morah.webhook.segredo}")
    private String segredoWebhook;

    private String baixaManual() throws Exception {
        return json("meio", "transferencia", "valor", 500, "pagoEm", "2026-10-05T12:00:00Z");
    }

    /** Síndica cria uma taxa e gera as cobranças; devolve o id da taxa. */
    private long criarTaxaEGerar(String token, int valor, String tipoRateio) throws Exception {
        String corpo = mockMvc.perform(comToken(post("/financeiro/taxas"), token,
                        json("descricao", "Taxa extra", "competencia", "2026-11", "valor", valor,
                                "tipoRateio", tipoRateio, "vencimentoDia", 10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoRateio").value(tipoRateio))
                .andReturn().getResponse().getContentAsString();
        long taxaId = lerJson(corpo).get("id").asLong();

        mockMvc.perform(comToken(post("/financeiro/taxas/" + taxaId + "/gerar-cobrancas"), token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.taxaId").value(taxaId))
                .andExpect(jsonPath("$.totalGeradas").value(2))
                .andExpect(jsonPath("$.falhas.length()").value(0));
        return taxaId;
    }

    @Test
    void deveListarSoAsCobrancasDaUnidadeDoProprietario() throws Exception {
        // proprietário é cumulativo ao morador: acessa a rota de morador normalmente
        mockMvc.perform(comToken(get("/financeiro/cobrancas").param("unidadeId", "2"), token(PROPRIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].unidadeId").value(1))
                .andExpect(jsonPath("$.content[0].status").value("atrasado"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void deveRetornar403QuandoPortariaListaCobrancas() throws Exception {
        mockMvc.perform(comToken(get("/financeiro/cobrancas"), token(PORTARIA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar403QuandoMoradorLeCobrancaDeOutraUnidade() throws Exception {
        // cobrança 1 é do Apto 302; Camila mora no Apto 502
        mockMvc.perform(comToken(get("/financeiro/cobrancas/1"), token(MORADOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(get("/financeiro/cobrancas/2"), token(MORADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar403QuandoMoradorGeraCobrancas() throws Exception {
        mockMvc.perform(comToken(post("/financeiro/taxas/1/gerar-cobrancas"), token(MORADOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveGerarPixParaCobrancaEmAberto() throws Exception {
        mockMvc.perform(comToken(get("/financeiro/cobrancas/1/pix"), token(PROPRIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPix").isNotEmpty())
                .andExpect(jsonPath("$.expiraEm").isNotEmpty());
    }

    @Test
    void deveRetornar409NoPixDeCobrancaPaga() throws Exception {
        mockMvc.perform(comToken(post("/financeiro/cobrancas/1/baixa-manual"), token(SINDICO), baixaManual()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pago"));

        mockMvc.perform(comToken(get("/financeiro/cobrancas/1/pix"), token(PROPRIETARIO)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409NaBaixaManualDeCobrancaJaPaga() throws Exception {
        String token = token(SINDICO);
        mockMvc.perform(comToken(post("/financeiro/cobrancas/2/baixa-manual"), token, baixaManual()))
                .andExpect(status().isOk());
        mockMvc.perform(comToken(post("/financeiro/cobrancas/2/baixa-manual"), token, baixaManual()))
                .andExpect(status().isConflict());
    }

    @Test
    void deveGerarValoresDoRateioIgualitario() throws Exception {
        String token = token(SINDICO);
        criarTaxaEGerar(token, 1000, "igualitario");

        mockMvc.perform(comToken(get("/financeiro/cobrancas").param("competencia", "2026-11"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].valorOriginal").value(500.0))
                .andExpect(jsonPath("$.content[1].valorOriginal").value(500.0))
                .andExpect(jsonPath("$.content[0].multa").value(0.0))
                .andExpect(jsonPath("$.content[0].vencimento").value("2026-11-10"))
                .andExpect(jsonPath("$.content[0].status").value("pendente"));
    }

    @Test
    void deveGerarValoresDoRateioPorFracaoIdeal() throws Exception {
        String token = token(SINDICO);
        criarTaxaEGerar(token, 300, "fracao_ideal");

        // fração ideal 0.5 em cada unidade → 300 x 0.5 = 150
        mockMvc.perform(comToken(get("/financeiro/cobrancas").param("competencia", "2026-11"), token))
                .andExpect(jsonPath("$.content[0].valorOriginal").value(150.0))
                .andExpect(jsonPath("$.content[1].valorOriginal").value(150.0));
    }

    @Test
    void deveRegistrarFalhaQuandoUnidadeJaFoiCobradaPelaTaxa() throws Exception {
        String token = token(SINDICO);
        long taxaId = criarTaxaEGerar(token, 1000, "igualitario");

        mockMvc.perform(comToken(post("/financeiro/taxas/" + taxaId + "/gerar-cobrancas"), token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.totalGeradas").value(0))
                .andExpect(jsonPath("$.falhas.length()").value(2));
    }

    @Test
    void deveRetornar400QuandoCompetenciaForaDoFormato() throws Exception {
        mockMvc.perform(comToken(post("/financeiro/taxas"), token(SINDICO),
                        json("descricao", "x", "competencia", "09/2026", "valor", 10,
                                "tipoRateio", "igualitario", "vencimentoDia", 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("competencia"));
    }

    @Test
    void deveRetornar401NoWebhookComAssinaturaInvalida() throws Exception {
        String corpo = json("transacaoId", "tx-1", "cobrancaId", 1, "status", "confirmado");
        mockMvc.perform(post("/financeiro/webhooks/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .header(WebhookPagamentoProcessador.CABECALHO_ASSINATURA, "assinatura-falsa")
                        .content(corpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveMarcarCobrancaComoPagaNoWebhookComAssinaturaValida() throws Exception {
        String corpo = json("transacaoId", "tx-1", "cobrancaId", 1, "status", "confirmado");
        mockMvc.perform(post("/financeiro/webhooks/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .header(WebhookPagamentoProcessador.CABECALHO_ASSINATURA,
                                WebhookPagamentoProcessador.assinar(corpo, segredoWebhook))
                        .content(corpo))
                .andExpect(status().isAccepted());

        mockMvc.perform(comToken(get("/financeiro/cobrancas/1"), token(PROPRIETARIO)))
                .andExpect(jsonPath("$.status").value("pago"));
        mockMvc.perform(comToken(get("/notificacoes"), token(PROPRIETARIO)))
                .andExpect(jsonPath("$[0].tipo").value("financeiro"))
                .andExpect(jsonPath("$[0].referenciaId").value(1));
    }

    @Test
    void naoDeveAlterarCobrancaQuandoWebhookFalhou() throws Exception {
        String corpo = json("transacaoId", "tx-2", "cobrancaId", 1, "status", "falhou");
        mockMvc.perform(post("/financeiro/webhooks/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .header(WebhookPagamentoProcessador.CABECALHO_ASSINATURA,
                                WebhookPagamentoProcessador.assinar(corpo, segredoWebhook))
                        .content(corpo))
                .andExpect(status().isAccepted());

        mockMvc.perform(comToken(get("/financeiro/cobrancas/1"), token(PROPRIETARIO)))
                .andExpect(jsonPath("$.status").value("atrasado"));
    }

    @Test
    void deveCalcularResumoDeInadimplencia() throws Exception {
        // seed: as duas unidades têm cobrança vencida em 10/09/2026 (atrasado)
        mockMvc.perform(comToken(get("/financeiro/inadimplencia"), token(SINDICO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUnidades").value(2))
                .andExpect(jsonPath("$.unidadesInadimplentes").value(2))
                .andExpect(jsonPath("$.percentualInadimplencia").value(100.0))
                .andExpect(jsonPath("$.valorTotalEmAberto").value(1000.0));
    }

    @Test
    void devePublicarEListarDocumentoFinanceiro() throws Exception {
        mockMvc.perform(comToken(post("/financeiro/documentos"), token(SINDICO),
                        json("tipo", "nota_fiscal", "titulo", "NF elevador", "arquivoUrl", "https://exemplo.com/nf.pdf")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicadoEm").isNotEmpty());

        mockMvc.perform(comToken(get("/financeiro/documentos"), token(MORADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("NF elevador"));
    }
}
