package com.proyectofs1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta de GET /api/v1/auth/validar: confirma de quién es el token.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SesionDTO {
    private String usuario;
    private String rol;
}