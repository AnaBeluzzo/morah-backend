package br.com.morah;

import br.com.morah.repository.login.PessoaPerfilRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UnidadeControllerTest extends IntegracaoTest {

    @Autowired
    private PessoaPerfilRepository pessoaPerfilRepository;

    private String inquilino() throws Exception {
        return json("nome", "Bruno Lima", "cpf", "123.456.789-00", "email", "bruno.lima@demo.com.br",
                "contratoInicio", "2026-10-01", "contratoFim", "2027-09-30");
    }

    @Test
    void deveBuscarAUnidadeDoContextoAtivo() throws Exception {
        mockMvc.perform(comToken(get("/unidades/minha"), token(MORADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.identificacao").value("Apto 502"))
                .andExpect(jsonPath("$.blocoNome").value("Bloco B"))
                .andExpect(jsonPath("$.fracaoIdeal").value(0.5));
    }

    @Test
    void deveRetornar403QuandoMoradorNaoProprietarioCadastraInquilino() throws Exception {
        mockMvc.perform(comToken(post("/unidades/minha/inquilinos"), token(MORADOR), inquilino()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveCadastrarEEncerrarInquilino() throws Exception {
        String token = token(PROPRIETARIO);
        String corpo = mockMvc.perform(comToken(post("/unidades/minha/inquilinos"), token, inquilino()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoVinculo").value("inquilino"))
                .andExpect(jsonPath("$.unidadeId").value(1))
                .andExpect(jsonPath("$.pessoa.nome").value("Bruno Lima"))
                .andReturn().getResponse().getContentAsString();
        long vinculoId = lerJson(corpo).get("id").asLong();
        long pessoaId = lerJson(corpo).get("pessoa").get("id").asLong();
        assertThat(pessoaPerfilRepository.findByPessoaIdOrderById(pessoaId)).hasSize(1);

        mockMvc.perform(comToken(delete("/unidades/minha/inquilinos/" + vinculoId), token))
                .andExpect(status().isNoContent());

        assertThat(pessoaPerfilRepository.findByPessoaIdOrderById(pessoaId)).isEmpty();
        mockMvc.perform(comToken(get("/unidades/minha/vinculos"), token))
                .andExpect(jsonPath("$[0].fim").value(LocalDate.now().toString()));
    }

    @Test
    void deveRetornar400QuandoInquilinoSemCpf() throws Exception {
        mockMvc.perform(comToken(post("/unidades/minha/inquilinos"), token(PROPRIETARIO),
                        json("nome", "Sem CPF", "contratoInicio", "2026-10-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("cpf"));
    }

    @Test
    void deveAprovarSolicitacaoDeVinculoERecusarSegundaDecisao() throws Exception {
        String corpo = mockMvc.perform(comToken(post("/unidades/minha/vinculos/solicitacoes"), token(MORADOR),
                        json("tipoVinculo", "dependente", "nome", "Lia Duarte")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("pendente"))
                .andReturn().getResponse().getContentAsString();
        long solicitacaoId = lerJson(corpo).get("id").asLong();

        String sindico = token(SINDICO);
        mockMvc.perform(comToken(get("/unidades/2/vinculos/solicitacoes"), sindico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeSolicitado").value("Lia Duarte"));

        String url = "/unidades/2/vinculos/solicitacoes/" + solicitacaoId;
        mockMvc.perform(comToken(patch(url), sindico, json("decisao", "aprovada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("aprovada"));
        mockMvc.perform(comToken(patch(url), sindico, json("decisao", "rejeitada")))
                .andExpect(status().isConflict());

        mockMvc.perform(comToken(get("/unidades/minha/vinculos"), token(MORADOR)))
                .andExpect(jsonPath("$[0].pessoa.nome").value("Lia Duarte"))
                .andExpect(jsonPath("$[0].tipoVinculo").value("dependente"));
    }

    @Test
    void deveRetornar404QuandoSindicoBuscaUnidadeInexistente() throws Exception {
        String sindico = token(SINDICO);
        mockMvc.perform(comToken(get("/unidades/1"), sindico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("Apto 302"));
        mockMvc.perform(comToken(get("/unidades/999"), sindico))
                .andExpect(status().isNotFound());
    }
}
