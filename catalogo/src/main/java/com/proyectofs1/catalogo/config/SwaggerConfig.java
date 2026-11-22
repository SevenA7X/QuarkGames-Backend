package com.proyectofs1.catalogo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
        // URL relativa: Swagger usa el mismo host y protocolo de la página (https en EC2, http en local)
        .addServersItem(new Server().url("/"))
            .info(new Info()
                .title("API Catálogo de Juegos")
                .version("1.0")
                .description("Documentación de la API para la gestión del catálogo de videojuegos y categorías."
                    + "Ver juegos y categorías es público. Crear, editar y eliminar requiere un JWT de ADMIN: "
                        + "haz login en el MS Auth y pega el token en el botón Authorize (sin la palabra Bearer)."))
                // Botón "Authorize" para pegar el token del MS Auth
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // Swagger envía el token en todas las peticiones (los GET lo ignoran)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}