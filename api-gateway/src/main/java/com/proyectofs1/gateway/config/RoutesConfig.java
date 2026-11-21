package com.proyectofs1.gateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

// Configuración del API Gateway en Java.
// React conoce una sola URL base: http://localhost:8080/api/v1. Según el camino de la petición,
// el Gateway elige el microservicio de destino y le reenvía la solicitud SIN cambiar la ruta.
//   GET http://localhost:8080/api/v1/juegos/5  ->  http://localhost:8081/api/v1/juegos/5
// El Gateway solo enruta: la identidad y los permisos los valida cada microservicio.
@Configuration
public class RoutesConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
                               @Value("${app.catalogo-url}") String catalogo,
                               @Value("${app.carrito-url}") String carrito,
                               @Value("${app.auth-url}") String auth) {
        return builder.routes()
                // ===== Rutas de la API =====
                .route("catalogo", r -> r.path("/api/v1/juegos/**", "/api/v1/categorias/**").uri(catalogo))
                .route("carrito", r -> r.path("/api/v1/carrito/**").uri(carrito))
                .route("auth", r -> r.path("/api/v1/auth/**").uri(auth))

                // ===== Documentación (Swagger unificado en http://localhost:8080/swagger-ui.html) =====
                // /openapi/<servicio> se traduce a /v3/api-docs del microservicio (setPath cambia la ruta).
                // preserveHostHeader: el microservicio "ve" localhost:8080 y publica esa URL en su
                // documentación, así "Try it out" también pasa por el Gateway.
                .route("docs-catalogo", r -> r.path("/openapi/catalogo")
                        .filters(f -> f.preserveHostHeader().setPath("/v3/api-docs")).uri(catalogo))
                .route("docs-carrito", r -> r.path("/openapi/carrito")
                        .filters(f -> f.preserveHostHeader().setPath("/v3/api-docs")).uri(carrito))
                .route("docs-auth", r -> r.path("/openapi/auth")
                        .filters(f -> f.preserveHostHeader().setPath("/v3/api-docs")).uri(auth))

                // OJO: no existe ninguna ruta para /internal/**. El endpoint interno del catálogo
                // (descontar stock) NO se publica: solo lo llama el MS Carrito directo al puerto 8081.
                .build();
    }

    // CORS: el navegador solo permite que React (http://localhost:5173) llame al Gateway (8080)
    // porque esta política lo autoriza. Postman y la comunicación entre microservicios no dependen de CORS.
    @Bean
    public CorsWebFilter cors(@Value("${app.allowed-origins}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origenes.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cabeceras que usa la SPA: JSON, el token JWT del admin y el identificador del carrito anónimo
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Carrito-Id"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
