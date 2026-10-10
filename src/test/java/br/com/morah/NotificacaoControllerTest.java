package br.com.morah;

import br.com.morah.dto.notificacoes.TipoNotificacao;
import br.com.morah.model.notificacoes.Notificacao;
import br.com.morah.repository.login.PessoaRepository;
import br.com.morah.repository.notificacoes.NotificacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificacaoControllerTest extends IntegracaoTest {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    /** Cria uma notificação para o proprietário Rafael (pessoa 2). */
    private Long criarNotificacaoDoRafael() {
        Notificacao notificacao = new Notificacao();
        notificacao.setPessoa(pessoaRepository.findById(2L).orElseThrow());
        notificacao.setTipo(TipoNotificacao.AVISO);
        notificacao.setTitulo("Teste");
        notificacao.setMensagem("Mensagem de teste");
        notificacao.setEnviadaEm(Instant.now());
        return notificacaoRepository.save(notificacao).getId();
    }

    @Test
    void deveListarAsNotificacoesDaPessoaComoArray() throws Exception {
        criarNotificacaoDoRafael();

        mockMvc.perform(comToken(get("/notificacoes"), token(PROPRIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("aviso"))
                .andExpect(jsonPath("$[0].titulo").value("Teste"))
                .andExpect(jsonPath("$[0].lidaEm").doesNotExist());
    }

    @Test
    void deveMarcarNotificacaoComoLida() throws Exception {
        Long id = criarNotificacaoDoRafael();

        mockMvc.perform(comToken(patch("/notificacoes/" + id + "/lida"), token(PROPRIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lidaEm").isNotEmpty());
    }

    @Test
    void deveRetornar404AoMarcarNotificacaoDeOutraPessoa() throws Exception {
        Long id = criarNotificacaoDoRafael();

        mockMvc.perform(comToken(patch("/notificacoes/" + id + "/lida"), token(MORADOR)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoSizeMaiorQue100() throws Exception {
        mockMvc.perform(comToken(get("/notificacoes").param("size", "101"), token(MORADOR)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("size"));
    }
}
