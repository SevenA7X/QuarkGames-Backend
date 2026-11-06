package com.proyectofs1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta del login: React guarda "token" y lo envía en "Authorization: Bearer <token>"
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String tipo;       // siempre "Bearer"
    private String usuario;
    private String rol;
    private long expiraEnMs;   // duración del token, para que React sepa cuándo vence
}