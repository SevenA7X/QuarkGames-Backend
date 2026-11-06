package com.proyectofs1.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

// Prueba unitaria pura: JwtService no tiene dependencias, se crea con "new".
class JwtServiceTest {

    private static final String SECRETO = "WZ6zGZwI3mF/BibbuzY/pvQx2EQ+qjXOlOkJVSlvupeHfOj+kOlmGvZRkDcgsrgp";
    private static final String OTRO_SECRETO = "c2VjcmV0by1kaXN0aW50by1wYXJhLXByb2Jhci1sYS1maXJtYS1kZWwtdG9rZW4=";

    private final JwtService jwtService = new JwtService(SECRETO, 3600000L);

    @Test
    void generaUnTokenConUsuarioYRol() {
        String token = jwtService.generarToken("admin", "ADMIN");

        Claims claims = jwtService.validarToken(token);

        assertThat(token.split("\\.")).hasSize(3); // header.payload.firma
        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get("rol", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        String tokenAjeno = new JwtService(OTRO_SECRETO, 3600000L).generarToken("admin", "ADMIN");

        assertThatThrownBy(() -> jwtService.validarToken(tokenAjeno))
                .isInstanceOf(SignatureException.class);
        assertThat(jwtService.obtenerClaimsSiEsValido(tokenAjeno)).isNull();
    }

    @Test
    void rechazaTokenExpirado() {
        // Expiración negativa: el token nace vencido
        String vencido = new JwtService(SECRETO, -1000L).generarToken("admin", "ADMIN");

        assertThatThrownBy(() -> jwtService.validarToken(vencido))
                .isInstanceOf(ExpiredJwtException.class);
        assertThat(jwtService.obtenerClaimsSiEsValido(vencido)).isNull();
    }

    @Test
    void devuelveNullConTextoQueNoEsToken() {
        assertThat(jwtService.obtenerClaimsSiEsValido("esto-no-es-un-jwt")).isNull();
        assertThat(jwtService.obtenerClaimsSiEsValido("")).isNull();
    }
}