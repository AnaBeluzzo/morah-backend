package br.com.morah;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integração: sobe a aplicação com o SQLite de teste e o seed do data.sql.
 * Cada teste roda numa transação desfeita ao final, então um teste não interfere no outro.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegracaoTest {

    protected static final String SINDICO = "helena.prado@demo.com.br";
    protected static final String PROPRIETARIO = "rafael.nunes@demo.com.br";
    protected static final String MORADOR = "camila.duarte@demo.com.br";
    protected static final String PORTARIA = "portaria@demo.com.br";
    protected static final String SENHA = "demo123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /** Faz o login de verdade (POST /auth/login) e devolve o corpo da resposta. */
    protected JsonNode login(String email) throws Exception {
        String corpo = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", email, "senha", SENHA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corpo);
    }

    protected String token(String email) throws Exception {
        return login(email).get("accessToken").asText();
    }

    /** Adiciona o cabeçalho Authorization e, se houver corpo, o JSON. */
    protected MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder requisicao, String token) {
        return requisicao.header("Authorization", "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder requisicao, String token, String json) {
        return comToken(requisicao, token).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    /** Monta um JSON simples a partir de pares chave/valor: json("a", 1, "b", "x"). */
    protected String json(Object... chavesEValores) throws Exception {
        java.util.Map<String, Object> mapa = new java.util.LinkedHashMap<>();
        for (int i = 0; i < chavesEValores.length; i += 2) {
            mapa.put((String) chavesEValores[i], chavesEValores[i + 1]);
        }
        return objectMapper.writeValueAsString(mapa);
    }

    protected JsonNode lerJson(String corpo) throws Exception {
        return objectMapper.readTree(corpo);
    }
}
