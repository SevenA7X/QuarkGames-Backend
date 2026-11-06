package com.proyectofs1.auth.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Formato único de error para todo el microservicio.
// Ej.: { "status": 401, "mensaje": "Usuario o contraseña incorrectos.", "timestamp": "..." }
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDTO {
    private int status;
    private String mensaje;
    private LocalDateTime timestamp;
    // Solo en errores de validación: { "usuario": "Ingresa tu usuario." }
    private Map<String, String> errores;

    public ErrorDTO(int status, String mensaje) {
        this(status, mensaje, LocalDateTime.now(), null);
    }
}