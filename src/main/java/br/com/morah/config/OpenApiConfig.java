package br.com.morah.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI (http://localhost:8080/v1/swagger-ui.html) para testar as APIs no navegador.
 * O botão "Authorize" recebe o accessToken devolvido por POST /auth/login.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI morahOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Morah API — Gestão Condominial").version("2.0.0"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
