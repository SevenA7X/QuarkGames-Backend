package com.proyectofs1.carrito.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Carrito - Quark Games")
                        .version("1.0")
                        .description("Carrito de compras anónimo. Cada navegador se identifica con la "
                                + "cabecera X-Carrito-Id (un UUID que genera React). Valida el stock "
                                + "contra el MS Catálogo y descuenta el stock al comprar."));
    }
}