package br.com.morah;

import br.com.morah.model.AutorizacaoVisita;
import br.com.morah.repository.AutorizacaoVisitaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortariaControllerTest extends IntegracaoTest {

    @Autowired
    private AutorizacaoVisitaRepository autorizacaoVisitaRepository;

    private String visitante() throws Exception {
        return json("unidadeId", 1, "visitante", Map.of("nome", "Joana Reis", "documento", "RG 123"), "motivo", "Visita");
    }

    @Test
    void deveRetornar403QuandoMoradorRegistraVisitante() throws Exception {
        mockMvc.perform(comToken(post("/portaria/visitantes"), token(MORADOR), visitante()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRegistrarVisitanteENotificarAUnidade() throws Exception {
        mockMvc.perform(comToken(post("/portaria/visitantes"), token(PORTARIA), visitante()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pendente"))
                .andExpect(jsonPath("$.unidadeId").value(1))
                .andExpect(jsonPath("$.visitante.nome").value("Joana Reis"));

        mockMvc.perform(comToken(get("/notificacoes"), token(PROPRIETARIO)))
                .andExpect(jsonPath("$[0].tipo").value("portaria"));
    }

    @Test
    void deveRetornar409QuandoAutorizacaoJaDecidida() throws Exception {
        String token = token(PROPRIETARIO);
        mockMvc.perform(comToken(patch("/portaria/autorizacoes/1/decisao"), token, json("decisao", "autorizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("autorizada"))
                .andExpect(jsonPath("$.respondidoEm").isNotEmpty());

        mockMvc.perform(comToken(patch("/portaria/autorizacoes/1/decisao"), token, json("decisao", "recusado")))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409QuandoAutorizacaoExpirada() throws Exception {
        AutorizacaoVisita autorizacao = autorizacaoVisitaRepository.findById(1L).orElseThrow();
        autorizacao.setSolicitadoEm(Instant.now().minusSeconds(31 * 60));

        mockMvc.perform(comToken(get("/portaria/autorizacoes/1"), token(PROPRIETARIO)))
                .andExpect(jsonPath("$.status").value("expirada"));
        mockMvc.perform(comToken(patch("/portaria/autorizacoes/1/decisao"), token(PROPRIETARIO), json("decisao", "autorizado")))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar403QuandoMoradorDecideAutorizacaoDeOutraUnidade() throws Exception {
        mockMvc.perform(comToken(patch("/portaria/autorizacoes/1/decisao"), token(MORADOR), json("decisao", "autorizado")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveFiltrarAutorizacoesPendentes() throws Exception {
        mockMvc.perform(comToken(get("/portaria/autorizacoes").param("status", "pendente"), token(PORTARIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
        mockMvc.perform(comToken(get("/portaria/autorizacoes"), token(MORADOR)))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void deveRegistrarEntradaESaidaDoVisitante() throws Exception {
        mockMvc.perform(comToken(patch("/portaria/autorizacoes/1/decisao"), token(PROPRIETARIO), json("decisao", "autorizado")))
                .andExpect(status().isOk());

        String portaria = token(PORTARIA);
        mockMvc.perform(comToken(post("/portaria/acessos"), portaria, json("autorizacaoId", 1, "visitanteId", 1, "tipo", "entrada")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("no_condominio"))
                .andExpect(jsonPath("$.autorizacaoId").value(1));
        mockMvc.perform(comToken(post("/portaria/acessos"), portaria, json("visitanteId", 1, "tipo", "saida")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("saiu"))
                .andExpect(jsonPath("$.saida").isNotEmpty());
        mockMvc.perform(comToken(post("/portaria/acessos"), portaria, json("visitanteId", 1, "tipo", "saida")))
                .andExpect(status().isConflict());

        mockMvc.perform(comToken(get("/portaria/acessos").param("data", LocalDate.now().toString()), token(SINDICO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void deveRetornar409NaEntradaComAutorizacaoPendente() throws Exception {
        mockMvc.perform(comToken(post("/portaria/acessos"), token(PORTARIA), json("autorizacaoId", 1, "visitanteId", 1, "tipo", "entrada")))
                .andExpect(status().isConflict());
    }

    @Test
    void deveReenviarSomenteAutorizacaoPendente() throws Exception {
        String portaria = token(PORTARIA);
        mockMvc.perform(comToken(post("/portaria/autorizacoes/1/reenviar"), portaria))
                .andExpect(status().isAccepted());

        mockMvc.perform(comToken(patch("/portaria/autorizacoes/1/decisao"), token(PROPRIETARIO), json("decisao", "recusado")))
                .andExpect(status().isOk());
        mockMvc.perform(comToken(post("/portaria/autorizacoes/1/reenviar"), portaria))
                .andExpect(status().isConflict());
    }
}
