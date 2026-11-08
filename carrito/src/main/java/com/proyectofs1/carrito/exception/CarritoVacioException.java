package com.proyectofs1.carrito.exception;

// 400: se intenta comprar con el carrito vacío (regla de negocio 1 de COMPRAR en el reducer)
public class CarritoVacioException extends RuntimeException {

    public CarritoVacioException() {
        super("El carrito está vacío.");
    }
}