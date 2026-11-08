package com.proyectofs1.carrito.exception;

// 404: el juego no existe en el MS Catálogo
public class JuegoNoEncontradoException extends RuntimeException {

    public JuegoNoEncontradoException(Long juegoId) {
        super("El juego con id " + juegoId + " no existe en el catálogo.");
    }
}