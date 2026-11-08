package com.proyectofs1.carrito.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.proyectofs1.carrito.dto.ErrorDTO;

// Captura las excepciones de todos los controladores y responde siempre con ErrorDTO
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===== 400 Bad Request =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        log.warn("Error de validación: {}", errores);
        return ResponseEntity.badRequest()
                .body(new ErrorDTO(400, "Datos inválidos.", LocalDateTime.now(), errores, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorDTO(400, "El cuerpo de la petición no es un JSON válido."));
    }

    // Falta la cabecera X-Carrito-Id
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorDTO> manejarCabeceraFaltante(MissingRequestHeaderException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorDTO(400, "Falta la cabecera " + ex.getHeaderName() + "."));
    }

    // Ej.: /items/abc cuando se esperaba un número
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorDTO(400, "El parámetro '" + ex.getName() + "' tiene un formato inválido."));
    }

    @ExceptionHandler({CarritoIdInvalidoException.class, CarritoVacioException.class})
    public ResponseEntity<ErrorDTO> manejarPeticionInvalida(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ErrorDTO(400, ex.getMessage()));
    }

    // ===== 404 Not Found =====

    @ExceptionHandler({JuegoNoEncontradoException.class, ItemNoEncontradoException.class})
    public ResponseEntity<ErrorDTO> manejarNoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(404, ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDTO> manejarRutaInexistente(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDTO(404, "El recurso solicitado no existe."));
    }

    // ===== 405 Method Not Allowed =====

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDTO> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorDTO(405, "Método HTTP no permitido para esta ruta."));
    }

    // ===== 409 Conflict: reglas de stock =====

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorDTO> manejarStock(StockInsuficienteException ex) {
        var detalles = ex.getDetalles().isEmpty() ? null : ex.getDetalles();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDTO(409, ex.getMessage(), LocalDateTime.now(), null, detalles));
    }

    // ===== 503 Service Unavailable: el catálogo no responde =====

    @ExceptionHandler(CatalogoNoDisponibleException.class)
    public ResponseEntity<ErrorDTO> manejarCatalogoCaido(CatalogoNoDisponibleException ex) {
        log.error("No se pudo contactar al MS Catálogo: {}", ex.getCause() != null ? ex.getCause().getMessage() : "");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(503, ex.getMessage()));
    }

    // ===== 500: cualquier otro error. No exponemos detalles internos =====

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> manejarGeneral(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDTO(500, "Ocurrió un error interno en el servidor."));
    }
}