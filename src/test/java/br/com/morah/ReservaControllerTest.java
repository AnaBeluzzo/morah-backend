package br.com.morah;

import br.com.morah.repository.OcorrenciaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservaControllerTest extends IntegracaoTest {

    private static final LocalDate AMANHA = LocalDate.now().plusDays(1);

    @Autowired
    private OcorrenciaRepository ocorrenciaRepository;

    /** Instante ISO de amanhã na hora/minuto pedidos (fuso do servidor). */
    private String amanha(int hora, int minuto) {
        return AMANHA.atTime(hora, minuto).atZone(ZoneId.systemDefault()).toInstant().toString();
    }

    private long criarReserva(String token, int horaInicio, int horaFim) throws Exception {
        String corpo = mockMvc.perform(comToken(post("/reservas"), token,
                        json("areaComumId", 1, "inicio", amanha(horaInicio, 0), "fim", amanha(horaFim, 0))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("confirmada"))
                .andExpect(jsonPath("$.valor").value(150.0))
                .andExpect(jsonPath("$.areaComumNome").value("Salão de festas"))
                .andReturn().getResponse().getContentAsString();
        return lerJson(corpo).get("id").asLong();
    }

    @Test
    void deveListarAreasComuns() throws Exception {
        mockMvc.perform(comToken(get("/areas-comuns"), token(PORTARIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Salão de festas"));
    }

    @Test
    void deveCriarReservaENotificarOSolicitante() throws Exception {
        String token = token(MORADOR);
        criarReserva(token, 10, 12);

        mockMvc.perform(comToken(get("/notificacoes"), token))
                .andExpect(jsonPath("$[0].tipo").value("reserva"));
    }

    @Test
    void deveRetornar409QuandoReservaSobrepoeOutra() throws Exception {
        criarReserva(token(MORADOR), 10, 12);

        mockMvc.perform(comToken(post("/reservas"), token(PROPRIETARIO),
                        json("areaComumId", 1, "inicio", amanha(11, 0), "fim", amanha(13, 0))))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoReservaForaDeHoraCheia() throws Exception {
        mockMvc.perform(comToken(post("/reservas"), token(MORADOR),
                        json("areaComumId", 1, "inicio", amanha(10, 30), "fim", amanha(11, 30))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(comToken(post("/reservas"), token(MORADOR),
                        json("areaComumId", 1, "inicio", amanha(6, 0), "fim", amanha(8, 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveMostrarHorariosOcupadosNaDisponibilidade() throws Exception {
        criarReserva(token(MORADOR), 10, 12);

        mockMvc.perform(comToken(get("/areas-comuns/1/disponibilidade").param("data", AMANHA.toString()), token(PROPRIETARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horarios.length()").value(14))
                .andExpect(jsonPath("$.horarios[1].disponivel").value(true))   // 09h
                .andExpect(jsonPath("$.horarios[2].disponivel").value(false))  // 10h
                .andExpect(jsonPath("$.horarios[3].disponivel").value(false))  // 11h
                .andExpect(jsonPath("$.horarios[4].disponivel").value(true));  // 12h
    }

    @Test
    void deveRetornar403QuandoMoradorLeReservaDeOutraUnidade() throws Exception {
        long id = criarReserva(token(MORADOR), 10, 11);

        mockMvc.perform(comToken(get("/reservas/" + id), token(PROPRIETARIO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(get("/reservas/" + id), token(SINDICO)))
                .andExpect(status().isOk());
    }

    @Test
    void deveCancelarReservaUmaVez() throws Exception {
        String token = token(MORADOR);
        long id = criarReserva(token, 14, 15);

        mockMvc.perform(comToken(delete("/reservas/" + id), token))
                .andExpect(status().isNoContent());
        mockMvc.perform(comToken(delete("/reservas/" + id), token))
                .andExpect(status().isConflict());
    }

    @Test
    void deveCriarOcorrenciaNaVistoriaComDanoEConcluirNaSaida() throws Exception {
        long id = criarReserva(token(MORADOR), 18, 20);
        String portaria = token(PORTARIA);

        mockMvc.perform(comToken(post("/reservas/" + id + "/vistoria"), portaria,
                        json("momento", "entrada", "dano", true, "observacao", "Cadeira quebrada",
                                "fotosUrl", java.util.List.of("https://exemplo.com/foto1.jpg"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("confirmada"));
        assertThat(ocorrenciaRepository.countByReservaId(id)).isEqualTo(1);

        mockMvc.perform(comToken(post("/reservas/" + id + "/vistoria"), portaria, json("momento", "saida")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("concluida"));
        assertThat(ocorrenciaRepository.countByReservaId(id)).isEqualTo(1);
    }

    @Test
    void deveListarAsReservasDoDiaParaAPortaria() throws Exception {
        criarReserva(token(MORADOR), 10, 11);

        mockMvc.perform(comToken(get("/reservas").param("data", AMANHA.toString()), token(PORTARIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
        mockMvc.perform(comToken(get("/reservas"), token(PORTARIA)))
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
