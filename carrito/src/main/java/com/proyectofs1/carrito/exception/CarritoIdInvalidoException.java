package com.proyectofs1.carrito.exception;

// 400: la cabecera X-Carrito-Id no es un UUID válido
public class CarritoIdInvalidoException extends RuntimeException {

    public CarritoIdInvalidoException() {
        super("La cabecera X-Carrito-Id debe ser un UUID válido.");
    }
}