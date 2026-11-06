package com.proyectofs1.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    public static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Autenticación - Quark Games")
                        .version("1.0")
                        .description("Login de administradores. Devuelve un JWT para usar en "
                                + "la cabecera Authorization: Bearer {token}. En Swagger: haz login, "
                                + "copia el token y pégalo en el botón Authorize (sin la palabra Bearer)."))
                // Agrega el botón "Authorize" en Swagger UI para pegar el token
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}