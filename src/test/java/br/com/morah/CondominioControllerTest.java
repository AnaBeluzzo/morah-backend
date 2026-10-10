package br.com.morah;

import br.com.morah.pattern.CacheModulosAtivos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CondominioControllerTest extends IntegracaoTest {

    /** O banco é desfeito ao fim de cada teste; o cache em memória também precisa ser. */
    @AfterEach
    void limparCache() {
        CacheModulosAtivos.INSTANCE.invalidar(1L);
    }

    @Test
    void deveBuscarPersonalizacaoComLogoNula() throws Exception {
        mockMvc.perform(comToken(get("/condominio/personalizacao"), token(MORADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeExibicao").value("Condomínio Demo"))
                .andExpect(jsonPath("$.corPrimaria").value("#1F4E79"))
                .andExpect(content().string(containsString("\"logoUrl\":null")));
    }

    @Test
    void deveRetornar403QuandoMoradorAlteraPersonalizacaoOuModulos() throws Exception {
        String token = token(MORADOR);
        mockMvc.perform(comToken(patch("/condominio/personalizacao"), token, json("corPrimaria", "#000000")))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(patch("/condominio/modulos/encomendas"), token, json("ativo", false)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar400QuandoCorForaDoFormato() throws Exception {
        mockMvc.perform(comToken(patch("/condominio/personalizacao"), token(SINDICO), json("corPrimaria", "azul")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("corPrimaria"));
    }

    @Test
    void deveManterOsOutrosCamposNoPatchParcialDeCores() throws Exception {
        String token = token(SINDICO);
        mockMvc.perform(comToken(patch("/condominio/personalizacao"), token, json("logoUrl", "https://exemplo.com/logo.png")))
                .andExpect(jsonPath("$.logoUrl").value("https://exemplo.com/logo.png"));

        mockMvc.perform(comToken(patch("/condominio/personalizacao"), token, json("corPrimaria", "#0F766E", "corDestaque", "#F97316")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.corPrimaria").value("#0F766E"))
                .andExpect(jsonPath("$.corDestaque").value("#F97316"))
                .andExpect(jsonPath("$.corSecundaria").value("#F2F4F7"))
                .andExpect(jsonPath("$.nomeExibicao").value("Condomínio Demo"))
                .andExpect(jsonPath("$.logoUrl").value("https://exemplo.com/logo.png"));
    }

    @Test
    void deveRemoverALogoQuandoLogoUrlNuloForEnviado() throws Exception {
        String token = token(SINDICO);
        mockMvc.perform(comToken(patch("/condominio/personalizacao"), token, json("logoUrl", "https://exemplo.com/logo.png")))
                .andExpect(status().isOk());

        mockMvc.perform(comToken(patch("/condominio/personalizacao"), token, "{\"logoUrl\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoUrl").value(nullValue()));
    }

    @Test
    void deveListarOsSeisModulosComNome() throws Exception {
        mockMvc.perform(comToken(get("/condominio/modulos"), token(PORTARIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[4].modulo").value("minha_unidade"))
                .andExpect(jsonPath("$[4].nome").value("Minha Unidade"))
                .andExpect(jsonPath("$[4].ativo").value(true));
    }

    @Test
    void deveBloquearRotasDoModuloDesativadoEVoltarAoReativar() throws Exception {
        String sindico = token(SINDICO);
        String morador = token(MORADOR);

        mockMvc.perform(comToken(patch("/condominio/modulos/encomendas"), sindico, json("ativo", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.nome").value("Encomendas"));

        mockMvc.perform(comToken(get("/encomendas"), morador))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Módulo Encomendas desativado neste condomínio"));
        // vale até para o síndico
        mockMvc.perform(comToken(get("/encomendas"), sindico))
                .andExpect(status().isForbidden());
        // outros módulos continuam liberados
        mockMvc.perform(comToken(get("/avisos"), morador))
                .andExpect(status().isOk());

        mockMvc.perform(comToken(patch("/condominio/modulos/encomendas"), sindico, json("ativo", true)))
                .andExpect(status().isOk());
        mockMvc.perform(comToken(get("/encomendas"), morador))
                .andExpect(status().isOk());
    }
}
