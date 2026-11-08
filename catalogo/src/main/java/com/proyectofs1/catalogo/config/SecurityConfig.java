package com.proyectofs1.catalogo.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofs1.catalogo.security.JwtAuthFilter;

import jakarta.servlet.http.HttpServletResponse;

// Reglas de acceso del catálogo:
//  - Ver juegos y categorías (GET): PÚBLICO. Cualquiera puede ver la tienda.
//  - Crear, editar y eliminar (POST, PUT, DELETE): solo con JWT de ADMIN emitido por el MS Auth.
//  - /internal/**: no usa JWT; lo protege la clave X-Internal-Key (InternalStockController)
//    y además el API Gateway no lo publica.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, ObjectMapper objectMapper) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API REST sin cookies de sesión: CSRF no aplica
            .csrf(AbstractHttpConfigurer::disable)
            // CORS lo resuelve el API Gateway; si también se activa aquí, la cabecera llega duplicada
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                // Endpoint interno para el MS Carrito: lo protege X-Internal-Key, no el JWT
                .requestMatchers("/internal/**").permitAll()
                // Lectura pública de la tienda
                .requestMatchers(HttpMethod.GET, "/api/v1/juegos/**", "/api/v1/categorias/**").permitAll()
                // Escritura: solo ADMIN
                .requestMatchers("/api/v1/juegos/**", "/api/v1/categorias/**").hasRole("ADMIN")
                // Cualquier otra ruta: se exige ADMIN (si no existe, con token responde 404)
                .anyRequest().hasRole("ADMIN")
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        escribirError(res, 401, "Debes iniciar sesión como administrador."))
                .accessDeniedHandler((req, res, ex) ->
                        escribirError(res, 403, "No tienes permisos para esta acción."))
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void escribirError(HttpServletResponse res, int status, String mensaje) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getWriter(), Map.of("status", status, "mensaje", mensaje));
    }
}
