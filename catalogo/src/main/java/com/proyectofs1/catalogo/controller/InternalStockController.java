package com.proyectofs1.catalogo.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyectofs1.catalogo.dto.DescontarStockDTO;
import com.proyectofs1.catalogo.exception.AccesoInternoDenegadoException;
import com.proyectofs1.catalogo.service.JuegoService;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;

// Endpoints INTERNOS: solo para otros microservicios (el MS Carrito), nunca para React.
// Dos capas de protección:
//  1) La ruta empieza con /internal/ y el API Gateway NO la enruta: desde el puerto 8080 no existe.
//  2) Exige la cabecera X-Internal-Key con la clave compartida (internal.key).
// @Hidden: no aparece en Swagger, porque no es parte de la API pública.
@Hidden
@RestController
@RequestMapping("/internal/juegos")
public class InternalStockController {

    private final JuegoService juegoService;
    private final String claveInterna;

    public InternalStockController(JuegoService juegoService,
                                   @Value("${internal.key}") String claveInterna) {
        this.juegoService = juegoService;
        this.claveInterna = claveInterna;
    }

    // 204 = descontado | 400 = datos inválidos | 403 = clave incorrecta
    // 404 = juego no existe | 409 = stock insuficiente (y no se descuenta NADA)
    @PostMapping("/descontar-stock")
    public ResponseEntity<Void> descontarStock(
            @RequestHeader(value = "X-Internal-Key", defaultValue = "") String claveRecibida,
            @Valid @RequestBody DescontarStockDTO solicitud) {

        // MessageDigest.isEqual compara en tiempo constante: no revela cuántos caracteres
        // acertó un atacante según lo que tarda la respuesta (ataque de tiempo).
        boolean claveValida = MessageDigest.isEqual(
                claveInterna.getBytes(StandardCharsets.UTF_8),
                claveRecibida.getBytes(StandardCharsets.UTF_8));
        if (!claveValida) {
            throw new AccesoInternoDenegadoException();
        }

        juegoService.descontarStock(solicitud.getItems());
        return ResponseEntity.noContent().build();
    }
}
