package com.proyectofs1.carrito.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuerpo de PUT /api/v1/carrito/items/{juegoId} -> { "cantidad": 2 }
// Fija la cantidad exacta. Con 0 se quita el ítem.
// La acción RESTAR_DEL_CARRITO del reducer se traduce en: cantidad actual - 1.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarCantidadRequestDTO {

    @Schema(example = "2")
    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 0, message = "La cantidad no puede ser negativa.")
    @Max(value = 99, message = "La cantidad máxima es 99.")
    private Integer cantidad;
}