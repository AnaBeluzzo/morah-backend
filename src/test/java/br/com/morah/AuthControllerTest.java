package br.com.morah;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegracaoTest {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void deveFazerLoginERetornarTokensEContexto() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", PROPRIETARIO, "senha", SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.contexto.perfil").value("proprietario"))
                .andExpect(jsonPath("$.contexto.condominioId").value(1))
                .andExpect(jsonPath("$.contexto.condominioNome").value("Condomínio Demo"))
                .andExpect(jsonPath("$.contexto.unidadeId").value(1))
                .andExpect(jsonPath("$.perfisDisponiveis[0].perfil").value("proprietario"));
    }

    @Test
    void deveRetornar401QuandoSenhaErrada() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", SINDICO, "senha", "errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void deveRetornar400QuandoLoginSemEmail() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("senha", SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("email"));
    }

    @Test
    void deveRetornarMeComTokenValido() throws Exception {
        mockMvc.perform(comToken(get("/auth/me"), token(SINDICO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pessoa.nome").value("Helena Prado"))
                .andExpect(jsonPath("$.contextoAtivo.perfil").value("sindico"))
                .andExpect(jsonPath("$.contextoAtivo.unidadeId").doesNotExist());
    }

    @Test
    void deveRetornar401NoMeSemToken() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test
    void deveRetornar401NoMeComTokenAdulterado() throws Exception {
        String token = token(SINDICO);
        String adulterado = token.substring(0, token.length() - 4) + "abcd";
        mockMvc.perform(comToken(get("/auth/me"), adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401NoMeComTokenExpirado() throws Exception {
        String sessaoValida = login(SINDICO).get("accessToken").asText();
        String sid = decodificarSid(sessaoValida);
        Instant passado = Instant.now().minusSeconds(7200);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1").issuedAt(passado).expiresAt(passado.plusSeconds(3600))
                .claim("sid", sid).claim("perfil", "sindico").claim("condominio_id", 1L)
                .build();
        String expirado = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        mockMvc.perform(comToken(get("/auth/me"), expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403QuandoContextoSemVinculo() throws Exception {
        mockMvc.perform(comToken(post("/auth/contexto"), token(MORADOR), json("condominioId", 1, "perfil", "sindico")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveTrocarContextoParaUmVinculoExistente() throws Exception {
        mockMvc.perform(comToken(post("/auth/contexto"), token(MORADOR), json("condominioId", 1, "perfil", "morador")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.contexto.perfil").value("morador"))
                .andExpect(jsonPath("$.contexto.unidadeId").value(2));
    }

    @Test
    void deveRenovarTokenEInvalidarORefreshAntigo() throws Exception {
        String refreshAntigo = login(MORADOR).get("refreshToken").asText();

        String corpo = mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json("refreshToken", refreshAntigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.contexto.perfil").value("morador"))
                .andReturn().getResponse().getContentAsString();
        JsonNode novo = lerJson(corpo);

        // rotação: o refresh antigo não funciona mais
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json("refreshToken", refreshAntigo)))
                .andExpect(status().isUnauthorized());

        // o novo access token funciona
        mockMvc.perform(comToken(get("/auth/me"), novo.get("accessToken").asText()))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401ComRefreshInvalido() throws Exception {
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json("refreshToken", "nao-existe")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveInvalidarAccessTokenNoLogout() throws Exception {
        String token = token(PORTARIA);

        mockMvc.perform(comToken(post("/auth/logout"), token))
                .andExpect(status().isNoContent());

        mockMvc.perform(comToken(get("/auth/me"), token))
                .andExpect(status().isUnauthorized());
    }

    /** Lê a claim "sid" do payload do JWT (parte do meio, em Base64URL). */
    private String decodificarSid(String token) throws Exception {
        String payload = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        return lerJson(payload).get("sid").asText();
    }
}
