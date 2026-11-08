package com.proyectofs1.catalogo.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// El catálogo NO genera tokens: solo VALIDA los que firma el MS Auth.
// Como ambos comparten la misma clave (jwt.secret), basta con verificar la firma
// y la expiración localmente. No hace falta llamar a Auth en cada petición.
@Component
public class JwtValidator {

    private final SecretKey clave;

    public JwtValidator(@Value("${jwt.secret}") String secretoBase64) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
    }

    // Devuelve los datos del token (usuario, rol) o null si es inválido, alterado o expiró
    public Claims obtenerClaimsSiEsValido(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
