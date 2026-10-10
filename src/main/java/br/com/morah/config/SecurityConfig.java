package br.com.morah.config;

import br.com.morah.exception.GlobalExceptionHandler;
import br.com.morah.repository.login.SessaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Spring Security como "resource server" de JWT próprio (HS256, sem Authorization Server).
 * RBAC fica nos @PreAuthorize dos controllers (@EnableMethodSecurity).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final SecretKey chave;

    public SecurityConfig(ObjectMapper objectMapper, @Value("${morah.jwt.segredo}") String segredo) {
        this.objectMapper = objectMapper;
        this.chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public SecurityFilterChain filtrosDeSeguranca(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh").permitAll()
                        // o webhook é autenticado pela assinatura HMAC, não por JWT
                        .requestMatchers(HttpMethod.POST, "/financeiro/webhooks/pagamentos").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converterPerfisEmPapeis()))
                        .authenticationEntryPoint((requisicao, resposta, erro) -> escreverProblema(resposta,
                                HttpStatus.UNAUTHORIZED, "Token ausente, expirado, inválido ou de sessão encerrada")))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint((requisicao, resposta, erro) -> escreverProblema(resposta,
                                HttpStatus.UNAUTHORIZED, "Token ausente, expirado, inválido ou de sessão encerrada"))
                        .accessDeniedHandler((requisicao, resposta, erro) -> escreverProblema(resposta,
                                HttpStatus.FORBIDDEN, "O perfil ativo não tem permissão para esta operação")));
        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    /** Valida assinatura e expiração e, além disso, se a sessão (claim "sid") ainda existe. */
    @Bean
    public JwtDecoder jwtDecoder(SessaoRepository sessaoRepository) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> sessaoAtiva = jwt -> sessaoRepository.existsById(jwt.getClaimAsString("sid"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Sessão encerrada", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), sessaoAtiva));
        return decoder;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Claim "perfil" vira ROLE_<PERFIL>; proprietário também recebe ROLE_MORADOR (é cumulativo). */
    private JwtAuthenticationConverter converterPerfisEmPapeis() {
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(jwt -> {
            String perfil = jwt.getClaimAsString("perfil");
            List<GrantedAuthority> papeis = new ArrayList<>();
            papeis.add(new SimpleGrantedAuthority("ROLE_" + perfil.toUpperCase()));
            if ("proprietario".equals(perfil)) {
                papeis.add(new SimpleGrantedAuthority("ROLE_MORADOR"));
            }
            return papeis;
        });
        return conversor;
    }

    /** 401/403 da camada de segurança no mesmo formato ProblemDetail do resto da API. */
    private void escreverProblema(HttpServletResponse resposta, HttpStatus status, String detalhe) throws IOException {
        resposta.setStatus(status.value());
        resposta.setContentType("application/problem+json");
        resposta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resposta.getOutputStream(), GlobalExceptionHandler.criarProblema(status, detalhe));
    }
}
