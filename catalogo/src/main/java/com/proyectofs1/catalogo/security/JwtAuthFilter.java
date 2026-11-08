package com.proyectofs1.catalogo.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Mismo filtro que el MS Auth: si viene "Authorization: Bearer <token>" válido,
// marca la petición como autenticada con el rol del token (ROLE_ADMIN).
// Si no viene o es inválido, no hace nada: SecurityConfig decide si la ruta lo exige.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtValidator jwtValidator;

    public JwtAuthFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            Claims claims = jwtValidator.obtenerClaimsSiEsValido(cabecera.substring(PREFIJO.length()));
            if (claims != null && claims.get("rol", String.class) != null) {
                var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("rol", String.class)));
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, autoridades);
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        filterChain.doFilter(request, response);
    }
}
