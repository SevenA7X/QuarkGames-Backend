package com.proyectofs1.catalogo.exception;

// 403: alguien llamó a un endpoint /internal/ sin la clave correcta (X-Internal-Key)
public class AccesoInternoDenegadoException extends RuntimeException {

    public AccesoInternoDenegadoException() {
        super("Servicio no autorizado.");
    }
}
