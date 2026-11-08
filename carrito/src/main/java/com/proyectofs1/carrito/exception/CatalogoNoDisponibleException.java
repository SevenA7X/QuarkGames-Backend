package com.proyectofs1.carrito.exception;

// 503: el MS Catálogo está apagado o no responde a tiempo
public class CatalogoNoDisponibleException extends RuntimeException {

    public CatalogoNoDisponibleException(Throwable causa) {
        super("El catálogo no está disponible. Intenta nuevamente en unos minutos.", causa);
    }
}