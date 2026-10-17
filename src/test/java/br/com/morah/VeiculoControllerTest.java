package br.com.morah;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeiculoControllerTest extends IntegracaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String moto() throws Exception {
        return json("placa", "xyz-9a87", "modelo", "Honda CG 160", "cor", "Vermelha", "tipo", "moto");
    }

    @Test
    void aTabelaVemDoSchemaSqlENaoDoHibernate() {
        String ddl = jdbcTemplate.queryForObject("SELECT sql FROM sqlite_master WHERE name = 'veiculo'", String.class);
        // texto exato do schema.sql (o Hibernate geraria "create table" e "check (tipo in ('CARRO','MOTO'))")
        assertThat(ddl).contains("CREATE TABLE veiculo")
                .contains("placa VARCHAR(7) NOT NULL UNIQUE")
                .contains("CHECK (tipo IN ('CARRO', 'MOTO'))");
    }

    @Test
    void deveListarOsVeiculosDaUnidade() throws Exception {
        mockMvc.perform(comToken(get("/unidades/minha/veiculos"), token(PROPRIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].placa").value("ABC1D23"))
                .andExpect(jsonPath("$[0].tipo").value("carro"))
                .andExpect(jsonPath("$[0].unidadeId").value(1));

        mockMvc.perform(comToken(get("/unidades/minha/veiculos"), token(MORADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveCadastrarEditarEExcluir() throws Exception {
        String token = token(MORADOR);
        String corpo = mockMvc.perform(comToken(post("/unidades/minha/veiculos"), token, moto()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("XYZ9A87"))
                .andExpect(jsonPath("$.tipo").value("moto"))
                .andExpect(jsonPath("$.unidadeId").value(2))
                .andReturn().getResponse().getContentAsString();
        long veiculoId = lerJson(corpo).get("id").asLong();

        mockMvc.perform(comToken(patch("/unidades/minha/veiculos/" + veiculoId), token, json("cor", "Preta")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cor").value("Preta"))
                .andExpect(jsonPath("$.modelo").value("Honda CG 160"));
        mockMvc.perform(comToken(patch("/unidades/minha/veiculos/" + veiculoId), token, json("cor", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cor").doesNotExist());

        mockMvc.perform(comToken(delete("/unidades/minha/veiculos/" + veiculoId), token))
                .andExpect(status().isNoContent());
        mockMvc.perform(comToken(get("/unidades/minha/veiculos"), token))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRetornar409ParaPlacaRepetida() throws Exception {
        mockMvc.perform(comToken(post("/unidades/minha/veiculos"), token(MORADOR),
                        json("placa", "abc-1d23", "modelo", "Outro", "tipo", "carro")))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400ParaPlacaOuTipoInvalidos() throws Exception {
        mockMvc.perform(comToken(post("/unidades/minha/veiculos"), token(MORADOR),
                        json("placa", "12345", "modelo", "Fusca", "tipo", "carro")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("placa"));

        mockMvc.perform(comToken(post("/unidades/minha/veiculos"), token(MORADOR),
                        json("placa", "AAA1234", "modelo", "Fusca", "tipo", "caminhao")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar403ParaVeiculoDeOutraUnidade() throws Exception {
        mockMvc.perform(comToken(patch("/unidades/minha/veiculos/1"), token(MORADOR), json("cor", "Azul")))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(delete("/unidades/minha/veiculos/1"), token(MORADOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar404ParaVeiculoInexistente() throws Exception {
        mockMvc.perform(comToken(delete("/unidades/minha/veiculos/999"), token(PROPRIETARIO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar403ParaPortariaESindico() throws Exception {
        mockMvc.perform(comToken(get("/unidades/minha/veiculos"), token(PORTARIA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(post("/unidades/minha/veiculos"), token(SINDICO), moto()))
                .andExpect(status().isForbidden());
    }
}
