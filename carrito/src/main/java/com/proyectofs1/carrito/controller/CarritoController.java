package com.proyectofs1.carrito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyectofs1.carrito.dto.ActualizarCantidadRequestDTO;
import com.proyectofs1.carrito.dto.AgregarItemRequestDTO;
import com.proyectofs1.carrito.dto.CarritoResponseDTO;
import com.proyectofs1.carrito.dto.CompraResponseDTO;
import com.proyectofs1.carrito.dto.ErrorDTO;
import com.proyectofs1.carrito.service.CarritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/carrito")
@Tag(name = "Carrito", description = "Carrito de compras anónimo identificado por la cabecera X-Carrito-Id.")
public class CarritoController {

    static final String CABECERA = "X-Carrito-Id";
    private static final String DESC_CABECERA = "UUID del carrito, generado por React con crypto.randomUUID()";
    private static final String EJEMPLO_ID = "3f2b8c1e-6a4d-4e2b-9c7a-1d2e3f4a5b6c";

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    @Operation(summary = "Ver el carrito", description = "Si el carrito aún no existe, devuelve uno vacío.")
    @ApiResponse(responseCode = "200", description = "Carrito con sus ítems y total")
    @ApiResponse(responseCode = "400", description = "Falta X-Carrito-Id o no es un UUID",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<CarritoResponseDTO> obtener(
            @Parameter(description = DESC_CABECERA, example = EJEMPLO_ID)
            @RequestHeader(CABECERA) String carritoId) {
        return ResponseEntity.ok(carritoService.obtener(carritoId));
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar un juego", description = "Suma unidades del juego. No permite superar el stock del catálogo.")
    @ApiResponse(responseCode = "200", description = "Producto agregado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "404", description = "El juego no existe en el catálogo",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "409", description = "Stock insuficiente",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "503", description = "El catálogo no responde",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<CarritoResponseDTO> agregar(
            @Parameter(description = DESC_CABECERA, example = EJEMPLO_ID)
            @RequestHeader(CABECERA) String carritoId,
            @Valid @RequestBody AgregarItemRequestDTO datos) {
        int cantidad = datos.getCantidad() == null ? 1 : datos.getCantidad();
        return ResponseEntity.ok(carritoService.agregar(carritoId, datos.getJuegoId(), cantidad));
    }

    @PutMapping("/items/{juegoId}")
    @Operation(summary = "Cambiar la cantidad de un juego",
            description = "Fija la cantidad exacta. Con 0 se quita del carrito. Para restar 1, envía la cantidad actual - 1.")
    @ApiResponse(responseCode = "200", description = "Cantidad actualizada")
    @ApiResponse(responseCode = "404", description = "El juego no está en el carrito",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "409", description = "Stock insuficiente",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<CarritoResponseDTO> actualizarCantidad(
            @Parameter(description = DESC_CABECERA, example = EJEMPLO_ID)
            @RequestHeader(CABECERA) String carritoId,
            @PathVariable Long juegoId,
            @Valid @RequestBody ActualizarCantidadRequestDTO datos) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(carritoId, juegoId, datos.getCantidad()));
    }

    @DeleteMapping("/items/{juegoId}")
    @Operation(summary = "Quitar un juego del carrito", description = "Si el juego no estaba, no hace nada.")
    @ApiResponse(responseCode = "200", description = "Producto eliminado del carrito")
    public ResponseEntity<CarritoResponseDTO> quitar(
            @Parameter(description = DESC_CABECERA, example = EJEMPLO_ID)
            @RequestHeader(CABECERA) String carritoId,
            @PathVariable Long juegoId) {
        return ResponseEntity.ok(carritoService.quitar(carritoId, juegoId));
    }

    @DeleteMapping
    @Operation(summary = "Vaciar el carrito")
    @ApiResponse(responseCode = "200", description = "Carrito vaciado")
    public ResponseEntity<CarritoResponseDTO> vaciar(
            @Parameter(description = DESC_CABECERA, example = EJEMPLO_ID)
            @RequestHeader(CABECERA) String carritoId) {
        return ResponseEntity.ok(carritoService.vaciar(carritoId));
    }

    @PostMapping("/comprar")
    @Operation(summary = "Comprar",
            description = "Valida que el carrito no esté vacío y que TODOS los juegos tengan stock. "
                    + "Si se cumple, descuenta el stock en el catálogo y vacía el carrito.")
    @ApiResponse(responseCode = "200", description = "Compra simulada completada")
    @ApiResponse(responseCode = "400", description = "El carrito está vacío",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "409", description = "Stock insuficiente para completar la compra",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "503", description = "El catálogo no responde",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<CompraResponseDTO> comprar(
            @Parameter(description = DESC_CABECERA, example = EJEMPLO_ID)
            @RequestHeader(CABECERA) String carritoId) {
        return ResponseEntity.ok(carritoService.comprar(carritoId));
    }
}