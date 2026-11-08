package com.proyectofs1.catalogo.exception;

// 404: el juego no existe en el catálogo
public class JuegoNoEncontradoException extends RuntimeException {

    public JuegoNoEncontradoException(Long id) {
        super("No existe el juego con id " + id);
    }
}
