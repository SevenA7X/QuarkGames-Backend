package com.proyectofs1.auth.config;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofs1.auth.dto.ErrorDTO;
import com.proyectofs1.auth.security.JwtAuthFilter;

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
            // CORS lo resuelve el API Gateway (globalcors). Si lo activamos también aquí,
            // el navegador recibiría la cabecera Access-Control-Allow-Origin duplicada.
            .cors(AbstractHttpConfigurer::disable)
            // STATELESS: el servidor no guarda sesión, todo viaja en el JWT
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Página de error de Spring: sin esto un 400/500 se convertiría en 401
                .requestMatchers("/error").permitAll()
                // Todo lo demás exige token de un ADMIN
                .anyRequest().hasRole("ADMIN")
            )
            // Respuesta JSON cuando falta token o es inválido (en vez de la página por defecto)
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        escribirError(res, 401, "Token ausente, inválido o expirado."))
                .accessDeniedHandler((req, res, ex) ->
                        escribirError(res, 403, "No tienes permisos para este recurso."))
            )
            // Nuestro filtro JWT corre antes del filtro de usuario/clave de Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt: hash lento con "sal" aleatoria. Dos hashes de "1234" nunca son iguales,
    // pero passwordEncoder.matches("1234", hash) sí los reconoce.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void escribirError(jakarta.servlet.http.HttpServletResponse res, int status, String mensaje)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getWriter(),
                new ErrorDTO(status, mensaje, LocalDateTime.now(), null));
    }
}