package com.proyectofs1.auth.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// Genera y valida tokens JWT firmados con HMAC-SHA256.
// Un JWT tiene 3 partes separadas por puntos: header.payload.firma
// - payload (claims): sub = usuario, rol, iat = emitido, exp = expira
// - firma: garantiza que nadie modificó el payload sin conocer la clave secreta
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secretoBase64,
                      @Value("${jwt.expiracion-ms}") long expiracionMs) {
        // hmacShaKeyFor exige al menos 256 bits; si la clave es corta, falla al arrancar (mejor así)
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String usuario, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    // Verifica firma y expiración. Lanza JwtException si el token no sirve.
    public Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Versión "segura" para el filtro: devuelve null en lugar de lanzar excepción.
    public Claims obtenerClaimsSiEsValido(String token) {
        try {
            return validarToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }
}