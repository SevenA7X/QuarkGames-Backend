package com.proyectofs1.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Un juego y cuántas unidades descontar (lo envía el MS Carrito al comprar)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemStockDTO {

    @NotNull(message = "El id del juego es obligatorio")
    private Long juegoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima a descontar es 1")
    private Integer cantidad;
}
