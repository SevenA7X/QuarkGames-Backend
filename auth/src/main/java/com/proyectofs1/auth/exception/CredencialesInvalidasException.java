package com.proyectofs1.auth.exception;

// Se lanza cuando el usuario no existe o la clave no coincide.
// Usamos el MISMO mensaje en ambos casos para no revelar qué usuarios existen
// (evita la "enumeración de usuarios").
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos.");
    }
}