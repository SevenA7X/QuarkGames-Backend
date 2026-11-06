package com.proyectofs1.auth.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.proyectofs1.auth.dto.ErrorDTO;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody:
// captura las excepciones de TODOS los controladores y responde JSON.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400: falló @Valid (campos vacíos, muy largos, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        log.warn("Error de validación: {}", errores);
        ErrorDTO cuerpo = new ErrorDTO(400, "Datos inválidos.", LocalDateTime.now(), errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    // 400: el cuerpo no es JSON válido o no viene
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de la petición ilegible: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorDTO(400, "El cuerpo de la petición no es un JSON válido."));
    }

    // 401: usuario o clave incorrectos
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorDTO> manejarCredenciales(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDTO(401, ex.getMessage()));
    }

    // 500: cualquier otro error no controlado. No exponemos detalles internos al cliente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> manejarGeneral(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDTO(500, "Ocurrió un error interno en el servidor."));
    }
}