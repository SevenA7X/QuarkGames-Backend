package com.proyectofs1.carrito.exception;

// 404: se intenta cambiar la cantidad de un juego que no está en el carrito
public class ItemNoEncontradoException extends RuntimeException {

    public ItemNoEncontradoException(Long juegoId) {
        super("El juego con id " + juegoId + " no está en el carrito.");
    }
}