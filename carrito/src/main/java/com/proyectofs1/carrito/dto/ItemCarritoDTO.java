package com.proyectofs1.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Una línea del carrito tal como la recibe React
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoDTO {
    private Long juegoId;
    private String titulo;
    private String imagen;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}