package br.com.morah;

import br.com.morah.pattern.CacheModulosAtivos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerTest extends IntegracaoTest {

    @AfterEach
    void limparCache() {
        CacheModulosAtivos.INSTANCE.invalidar(1L);
    }

    @Test
    void deveDevolverOPerfilCorretoParaCadaUsuario() throws Exception {
        mockMvc.perform(comToken(get("/home/dashboard"), token(MORADOR)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("morador"));
        mockMvc.perform(comToken(get("/home/dashboard"), token(PROPRIETARIO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("proprietario"));
        mockMvc.perform(comToken(get("/home/dashboard"), token(SINDICO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("sindico"));
        mockMvc.perform(comToken(get("/home/dashboard"), token(PORTARIA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("portaria"));
    }

    @Test
    void deveMontarATelaDoProprietarioComOsDadosDaUnidade() throws Exception {
        mockMvc.perform(comToken(get("/home/dashboard"), token(PROPRIETARIO)))
                .andExpect(jsonPath("$.unidade.identificacao").value("Apto 302"))
                .andExpect(jsonPath("$.proximosBoletos[0].status").value("atrasado"))
                .andExpect(jsonPath("$.avisosRecentes[0].titulo").value("Bem-vindos ao Morah"))
                .andExpect(jsonPath("$.encomendasPendentes.length()").value(1))
                .andExpect(jsonPath("$.autorizacoesPendentes.length()").value(1))
                .andExpect(jsonPath("$.proximasReservas.length()").value(0));
    }

    @Test
    void deveMontarATelaDoSindicoEDaPortaria() throws Exception {
        mockMvc.perform(comToken(get("/home/dashboard"), token(SINDICO)))
                .andExpect(jsonPath("$.inadimplencia.totalUnidades").value(2))
                .andExpect(jsonPath("$.avisosPublicadosRecentes.length()").value(1))
                .andExpect(jsonPath("$.solicitacoesVinculoPendentes").value(0))
                .andExpect(jsonPath("$.ocorrenciasAbertas").value(0));

        mockMvc.perform(comToken(get("/home/dashboard"), token(PORTARIA)))
                .andExpect(jsonPath("$.autorizacoesPendentes.length()").value(1))
                .andExpect(jsonPath("$.reservasDoDia.length()").value(0))
                .andExpect(jsonPath("$.encomendasRecebidasHoje").value(1));
    }

    @Test
    void deveVirComEncomendasPendentesVazioQuandoModuloDesativado() throws Exception {
        String sindico = token(SINDICO);
        String proprietario = token(PROPRIETARIO);
        mockMvc.perform(comToken(patch("/condominio/modulos/encomendas"), sindico, json("ativo", false)))
                .andExpect(status().isOk());

        // a tela inicial nunca é bloqueada; só a seção do módulo vem vazia
        mockMvc.perform(comToken(get("/home/dashboard"), proprietario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encomendasPendentes.length()").value(0))
                .andExpect(jsonPath("$.autorizacoesPendentes.length()").value(1));

        mockMvc.perform(comToken(patch("/condominio/modulos/encomendas"), sindico, json("ativo", true)))
                .andExpect(status().isOk());
    }

    @Test
    void deveOmitirInadimplenciaQuandoFinanceiroDesativado() throws Exception {
        String sindico = token(SINDICO);
        mockMvc.perform(comToken(patch("/condominio/modulos/financeiro"), sindico, json("ativo", false)))
                .andExpect(status().isOk());

        mockMvc.perform(comToken(get("/home/dashboard"), sindico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inadimplencia").doesNotExist());

        mockMvc.perform(comToken(patch("/condominio/modulos/financeiro"), sindico, json("ativo", true)))
                .andExpect(status().isOk());
    }
}
