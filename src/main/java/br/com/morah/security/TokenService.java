package br.com.morah.security;

import br.com.morah.model.login.Sessao;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

/** Gera o access token JWT (HS256) a partir da sessão. */
@Service
public class TokenService {

    public static final long VALIDADE_SEGUNDOS = 3600;

    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String gerarAccessToken(Sessao sessao) {
        Instant agora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(sessao.getPessoa().getId().toString())
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(VALIDADE_SEGUNDOS))
                .claim("sid", sessao.getId())
                .claim("perfil", sessao.getPerfil().getValor())
                .claim("condominio_id", sessao.getCondominio().getId());
        // síndico e portaria não têm unidade: a claim fica de fora
        if (sessao.getUnidade() != null) {
            claims.claim("unidade_id", sessao.getUnidade().getId());
        }
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims.build())).getTokenValue();
    }
}
