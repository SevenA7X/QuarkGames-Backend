package com.proyectofs1.catalogo.exception;

// 409 Conflict: se intenta descontar más unidades de las que hay
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String titulo, int stock, int pedido) {
        super("Stock insuficiente para '" + titulo + "': quedan " + stock + ", se pidieron " + pedido);
    }
}
