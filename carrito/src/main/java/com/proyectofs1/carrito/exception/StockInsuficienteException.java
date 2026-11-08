package com.proyectofs1.carrito.exception;

import java.util.List;

// 409 Conflict: se pide más de lo que hay en stock.
// Mismos mensajes que el reducer de React:
//  - al agregar: "Stock insuficiente."
//  - al comprar: "Stock insuficiente para completar la compra."
public class StockInsuficienteException extends RuntimeException {

    private final List<String> detalles;

    public StockInsuficienteException(String mensaje) {
        this(mensaje, List.of());
    }

    public StockInsuficienteException(String mensaje, List<String> detalles) {
        super(mensaje);
        this.detalles = detalles;
    }

    public List<String> getDetalles() {
        return detalles;
    }
}