package com.proyectofs1.carrito.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta de POST /api/v1/carrito/comprar: resumen de lo que se compró
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponseDTO {
    private String mensaje;
    private List<ItemCarritoDTO> items;
    private Integer totalUnidades;
    private Double total;
}