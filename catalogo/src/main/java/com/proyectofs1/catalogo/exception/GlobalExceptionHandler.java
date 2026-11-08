package com.proyectofs1.catalogo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Manejo de errores de validación (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Error de validación en la petición: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 404: juego inexistente al descontar stock
    @ExceptionHandler(JuegoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleJuegoNoEncontrado(JuegoNoEncontradoException ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", 404, "mensaje", ex.getMessage()));
    }

    // 403: llamada a /internal/ sin la clave correcta
    @ExceptionHandler(AccesoInternoDenegadoException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoInterno(AccesoInternoDenegadoException ex) {
        log.warn("Intento de acceso a endpoint interno con clave inválida");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("status", 403, "mensaje", ex.getMessage()));
    }

    // 409: no alcanza el stock al descontar
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuficiente(StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", 409, "mensaje", ex.getMessage()));
    }

    // Manejo genérico para excepciones no controladas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralExceptions(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error interno en el servidor.");
    }
}