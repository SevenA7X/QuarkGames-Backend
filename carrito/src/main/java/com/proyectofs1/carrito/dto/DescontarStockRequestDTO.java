package com.proyectofs1.carrito.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuerpo que el carrito envía al MS Catálogo para descontar stock:
// { "items": [ { "juegoId": 1, "cantidad": 2 }, { "juegoId": 5, "cantidad": 1 } ] }
// Se envían TODOS los juegos juntos para que el catálogo descuente en UNA transacción.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescontarStockRequestDTO {
    private List<ItemStockDTO> items;
}