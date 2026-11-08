package com.proyectofs1.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Un juego y cuántas unidades descontar. Debe coincidir con el DTO del MS Catálogo.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemStockDTO {
    private Long juegoId;
    private Integer cantidad;
}