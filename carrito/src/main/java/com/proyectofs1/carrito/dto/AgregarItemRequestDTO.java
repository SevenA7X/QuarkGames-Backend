package com.proyectofs1.carrito.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuerpo de POST /api/v1/carrito/items -> { "juegoId": 1, "cantidad": 1 }
// Equivale a la acción AGREGAR_AL_CARRITO del reducer.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarItemRequestDTO {

    @Schema(example = "1")
    @NotNull(message = "El id del juego es obligatorio.")
    @Positive(message = "El id del juego debe ser positivo.")
    private Long juegoId;

    // Si React no la envía, se agrega 1 unidad (igual que el botón "Agregar")
    @Schema(example = "1", defaultValue = "1")
    @Min(value = 1, message = "La cantidad mínima es 1.")
    @Max(value = 99, message = "La cantidad máxima por operación es 99.")
    private Integer cantidad = 1;
}