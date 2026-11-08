package com.proyectofs1.catalogo.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuerpo de POST /api/v1/juegos/descontar-stock:
// { "items": [ { "juegoId": 1, "cantidad": 2 }, { "juegoId": 5, "cantidad": 1 } ] }
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescontarStockDTO {

    @NotEmpty(message = "Debe venir al menos un juego")
    private List<@Valid ItemStockDTO> items;
}
