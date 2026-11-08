package com.proyectofs1.carrito.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Mismo formato de error que el MS de autenticación:
// { "status": 409, "mensaje": "Stock insuficiente.", "timestamp": "..." }
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDTO {
    private int status;
    private String mensaje;
    private LocalDateTime timestamp;
    // Solo en errores de validación: { "juegoId": "El id del juego es obligatorio." }
    private Map<String, String> errores;
    // Solo al comprar sin stock: ["Elden Ring: pediste 3, quedan 2"]
    private List<String> detalles;

    public ErrorDTO(int status, String mensaje) {
        this(status, mensaje, LocalDateTime.now(), null, null);
    }
}