package br.com.morah;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvisoControllerTest extends IntegracaoTest {

    /** Síndica publica um aviso e devolve o id criado. */
    private long criarAviso(Map<String, Object> publicoAlvo) throws Exception {
        String corpo = mockMvc.perform(comToken(post("/avisos"), token(SINDICO),
                        json("titulo", "Manutenção", "conteudo", "Elevador parado", "categoria", "manutencao",
                                "publicoAlvo", publicoAlvo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor.nome").value("Helena Prado"))
                .andExpect(jsonPath("$.prioridade").value("normal"))
                .andReturn().getResponse().getContentAsString();
        return lerJson(corpo).get("id").asLong();
    }

    @Test
    void deveRetornar403QuandoMoradorCriaAviso() throws Exception {
        mockMvc.perform(comToken(post("/avisos"), token(MORADOR),
                        json("titulo", "x", "conteudo", "y", "publicoAlvo", Map.of("tipo", "condominio"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar400QuandoPublicoBlocoSemBlocoId() throws Exception {
        mockMvc.perform(comToken(post("/avisos"), token(SINDICO),
                        json("titulo", "x", "conteudo", "y", "publicoAlvo", Map.of("tipo", "bloco"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveMarcarAvisoComoLidoNoGet() throws Exception {
        String token = token(MORADOR);

        mockMvc.perform(comToken(get("/avisos"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].lidoPeloUsuario").value(false));

        mockMvc.perform(comToken(get("/avisos/1"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lidoPeloUsuario").value(true));

        mockMvc.perform(comToken(get("/avisos"), token))
                .andExpect(jsonPath("$.content[0].lidoPeloUsuario").value(true));
    }

    @Test
    void deveAlterarSomenteOCampoEnviadoNoPatch() throws Exception {
        long id = criarAviso(Map.of("tipo", "condominio"));

        mockMvc.perform(comToken(patch("/avisos/" + id), token(SINDICO), json("titulo", "Novo título")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Novo título"))
                .andExpect(jsonPath("$.conteudo").value("Elevador parado"))
                .andExpect(jsonPath("$.categoria").value("manutencao"));
    }

    @Test
    void deveRetornar410QuandoAvisoExpirado() throws Exception {
        long id = criarAviso(Map.of("tipo", "condominio"));

        mockMvc.perform(comToken(delete("/avisos/" + id), token(SINDICO)))
                .andExpect(status().isNoContent());

        mockMvc.perform(comToken(get("/avisos/" + id), token(MORADOR)))
                .andExpect(status().isGone());
    }

    @Test
    void deveRetornar403QuandoAvisoEhDeOutraUnidade() throws Exception {
        // aviso só para o Apto 302 (unidade 1); Camila mora no Apto 502
        long id = criarAviso(Map.of("tipo", "unidade", "unidadeId", 1));

        mockMvc.perform(comToken(get("/avisos/" + id), token(MORADOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(get("/avisos/" + id), token(PROPRIETARIO)))
                .andExpect(status().isOk());
    }

    @Test
    void deveFiltrarAvisosPorCategoria() throws Exception {
        criarAviso(Map.of("tipo", "condominio"));

        mockMvc.perform(comToken(get("/avisos").param("categoria", "manutencao"), token(PORTARIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void deveNotificarOPublicoAlvoAoCriarAviso() throws Exception {
        criarAviso(Map.of("tipo", "condominio"));

        mockMvc.perform(comToken(get("/notificacoes"), token(MORADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("aviso"));
    }
}
