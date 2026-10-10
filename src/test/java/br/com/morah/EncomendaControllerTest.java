package br.com.morah;

import br.com.morah.dto.StatusAutorizacaoRetirada;
import br.com.morah.repository.AutorizacaoRetiradaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EncomendaControllerTest extends IntegracaoTest {

    @Autowired
    private AutorizacaoRetiradaRepository autorizacaoRetiradaRepository;

    @Test
    void deveListarSoAsEncomendasDaPropriaUnidade() throws Exception {
        mockMvc.perform(comToken(get("/encomendas").param("status", "aguardando_retirada"), token(PROPRIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("aguardando_retirada"));
        mockMvc.perform(comToken(get("/encomendas"), token(MORADOR)))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void deveRetornar403QuandoMoradorLeEncomendaDeOutraUnidade() throws Exception {
        mockMvc.perform(comToken(get("/encomendas/1"), token(MORADOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveCriarNotificacaoAoRegistrarEncomenda() throws Exception {
        mockMvc.perform(comToken(post("/encomendas"), token(PORTARIA), json("unidadeId", 1, "transportadora", "Correios")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("aguardando_retirada"))
                .andExpect(jsonPath("$.codigoRetirada").value(org.hamcrest.Matchers.matchesPattern("\\d{6}")));

        mockMvc.perform(comToken(get("/notificacoes"), token(PROPRIETARIO)))
                .andExpect(jsonPath("$[0].tipo").value("encomenda"));
    }

    @Test
    void deveRetornar409NaRetiradaComCodigoInvalido() throws Exception {
        mockMvc.perform(comToken(post("/encomendas/1/retirada"), token(PORTARIA), json("codigo", "000000")))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetirarComOCodigoDaEncomendaUmaVez() throws Exception {
        String portaria = token(PORTARIA);
        String corpo = mockMvc.perform(comToken(get("/encomendas/1"), portaria))
                .andReturn().getResponse().getContentAsString();
        String codigo = lerJson(corpo).get("codigoRetirada").asText();

        mockMvc.perform(comToken(post("/encomendas/1/retirada"), portaria, json("codigo", codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("retirada"))
                .andExpect(jsonPath("$.retiradaEm").isNotEmpty());
        mockMvc.perform(comToken(post("/encomendas/1/retirada"), portaria, json("codigo", codigo)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetirarComCodigoDeTerceiroValido() throws Exception {
        String corpo = mockMvc.perform(comToken(post("/encomendas/1/autorizacoes-retirada"), token(PROPRIETARIO),
                        json("nomeTerceiro", "Pedro Alves", "documento", "RG 999")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ativa"))
                .andExpect(jsonPath("$.validade").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        long autorizacaoId = lerJson(corpo).get("id").asLong();
        String codigoTerceiro = lerJson(corpo).get("codigo").asText();

        mockMvc.perform(comToken(post("/encomendas/1/retirada"), token(PORTARIA),
                        json("codigo", codigoTerceiro, "retiradoPorNome", "Pedro Alves")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("retirada"));
        assertThat(autorizacaoRetiradaRepository.findById(autorizacaoId).orElseThrow().getStatus())
                .isEqualTo(StatusAutorizacaoRetirada.UTILIZADA);
    }

    @Test
    void deveRetornar403QuandoMoradorAutorizaTerceiroEmEncomendaDeOutraUnidade() throws Exception {
        mockMvc.perform(comToken(post("/encomendas/1/autorizacoes-retirada"), token(MORADOR),
                        json("nomeTerceiro", "X", "documento", "Y")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveAbrirOcorrenciaEMarcarEncomendaComoExtraviada() throws Exception {
        String token = token(PROPRIETARIO);
        mockMvc.perform(comToken(post("/encomendas/1/ocorrencias"), token,
                        json("titulo", "Encomenda sumiu", "descricao", "Não encontrada na portaria")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("aberta"))
                .andExpect(jsonPath("$.abertaEm").isNotEmpty());

        mockMvc.perform(comToken(get("/encomendas/1"), token))
                .andExpect(jsonPath("$.status").value("extraviada"));
    }
}
