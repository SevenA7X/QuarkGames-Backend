package com.proyectofs1.carrito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.proyectofs1.carrito.dto.DescontarStockRequestDTO;
import com.proyectofs1.carrito.dto.JuegoCatalogoDTO;

// Cliente REST DECLARATIVO: solo escribimos la "firma" de los endpoints del MS Catálogo
// y OpenFeign genera la implementación que hace las peticiones HTTP.
// La URL base se lee de application.properties (catalogo.url).
@FeignClient(name = "catalogo", url = "${catalogo.url}")
public interface CatalogoClient {

    // GET http://localhost:8081/api/v1/juegos/{id}  (endpoint PÚBLICO que ya existe en el catálogo)
    @GetMapping("/api/v1/juegos/{id}")
    JuegoCatalogoDTO obtenerJuego(@PathVariable("id") Long id);

    // POST http://localhost:8081/internal/juegos/descontar-stock  (endpoint INTERNO)
    // - Empieza con /internal/: el API Gateway NO lo enruta, así que no existe desde fuera.
    // - Además exige la cabecera X-Internal-Key con la clave compartida entre carrito y catálogo.
    @PostMapping("/internal/juegos/descontar-stock")
    void descontarStock(@RequestHeader("X-Internal-Key") String claveInterna,
                        @RequestBody DescontarStockRequestDTO solicitud);
}