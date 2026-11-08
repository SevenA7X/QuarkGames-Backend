package com.proyectofs1.carrito.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta de todos los endpoints del carrito (salvo comprar)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponseDTO {
    private String carritoId;
    private List<ItemCarritoDTO> items;
    private Integer totalUnidades;
    private Double total;
    private String mensaje;
}