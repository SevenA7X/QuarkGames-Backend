package com.proyectofs1.carrito.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyectofs1.carrito.dto.CarritoResponseDTO;
import com.proyectofs1.carrito.dto.CompraResponseDTO;
import com.proyectofs1.carrito.dto.ItemCarritoDTO;
import com.proyectofs1.carrito.exception.CarritoIdInvalidoException;
import com.proyectofs1.carrito.exception.CarritoVacioException;
import com.proyectofs1.carrito.exception.CatalogoNoDisponibleException;
import com.proyectofs1.carrito.exception.JuegoNoEncontradoException;
import com.proyectofs1.carrito.exception.StockInsuficienteException;
import com.proyectofs1.carrito.service.CarritoService;

// Capa web: rutas, cabecera X-Carrito-Id, validaciones y códigos HTTP.
// CarritoService es un mock.
@WebMvcTest(CarritoController.class)
class CarritoControllerTest {

    private static final String ID = "3f2b8c1e-6a4d-4e2b-9c7a-1d2e3f4a5b6c";

    @Autowired private MockMvc mockMvc;
    @MockBean private CarritoService carritoService;

    private CarritoResponseDTO carritoConElden(String mensaje) {
        var item = new ItemCarritoDTO(7L, "Elden Ring", "https://img", 49990.0, 1, 49990.0);
        return new CarritoResponseDTO(ID, List.of(item), 1, 49990.0, mensaje);
    }

    @Test
    void getDevuelveElCarrito() throws Exception {
        when(carritoService.obtener(ID)).thenReturn(carritoConElden(null));

        mockMvc.perform(get("/api/v1/carrito").header("X-Carrito-Id", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carritoId").value(ID))
                .andExpect(jsonPath("$.items[0].titulo").value("Elden Ring"))
                .andExpect(jsonPath("$.total").value(49990.0));
    }

    @Test
    void sinCabeceraResponde400() throws Exception {
        mockMvc.perform(get("/api/v1/carrito"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta la cabecera X-Carrito-Id."));
    }

    @Test
    void cabeceraQueNoEsUuidResponde400() throws Exception {
        when(carritoService.obtener("hola")).thenThrow(new CarritoIdInvalidoException());

        mockMvc.perform(get("/api/v1/carrito").header("X-Carrito-Id", "hola"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La cabecera X-Carrito-Id debe ser un UUID válido."));
    }

    @Test
    void agregarSinCantidadAgregaUnaUnidad() throws Exception {
        when(carritoService.agregar(ID, 7L, 1)).thenReturn(carritoConElden("Producto agregado."));

        mockMvc.perform(post("/api/v1/carrito/items").header("X-Carrito-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Producto agregado."));

        verify(carritoService).agregar(ID, 7L, 1);
    }

    @Test
    void agregarSinJuegoIdResponde400ConErrorPorCampo() throws Exception {
        mockMvc.perform(post("/api/v1/carrito/items").header("X-Carrito-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.juegoId").value("El id del juego es obligatorio."))
                .andExpect(jsonPath("$.errores.cantidad").value("La cantidad mínima es 1."));
    }

    @Test
    void agregarSinStockResponde409() throws Exception {
        when(carritoService.agregar(eq(ID), anyLong(), eq(1)))
                .thenThrow(new StockInsuficienteException("Stock insuficiente."));

        mockMvc.perform(post("/api/v1/carrito/items").header("X-Carrito-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":7,\"cantidad\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente."));
    }

    @Test
    void agregarJuegoInexistenteResponde404() throws Exception {
        when(carritoService.agregar(ID, 99L, 1)).thenThrow(new JuegoNoEncontradoException(99L));

        mockMvc.perform(post("/api/v1/carrito/items").header("X-Carrito-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void catalogoCaidoResponde503() throws Exception {
        when(carritoService.agregar(ID, 7L, 1))
                .thenThrow(new CatalogoNoDisponibleException(new RuntimeException("Connection refused")));

        mockMvc.perform(post("/api/v1/carrito/items").header("X-Carrito-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":7}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void putCambiaLaCantidad() throws Exception {
        when(carritoService.actualizarCantidad(ID, 7L, 0)).thenReturn(
                new CarritoResponseDTO(ID, List.of(), 0, 0.0, "Producto eliminado del carrito."));

        mockMvc.perform(put("/api/v1/carrito/items/7").header("X-Carrito-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void putConCantidadNegativaResponde400() throws Exception {
        mockMvc.perform(put("/api/v1/carrito/items/7").header("X-Carrito-Id", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cantidad").value("La cantidad no puede ser negativa."));
    }

    @Test
    void idDeJuegoQueNoEsNumeroResponde400() throws Exception {
        mockMvc.perform(delete("/api/v1/carrito/items/abc").header("X-Carrito-Id", ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parámetro 'juegoId' tiene un formato inválido."));
    }

    @Test
    void deleteItemYDeleteCarrito() throws Exception {
        var vacio = new CarritoResponseDTO(ID, List.of(), 0, 0.0, "Carrito vaciado.");
        when(carritoService.quitar(ID, 7L)).thenReturn(vacio);
        when(carritoService.vaciar(ID)).thenReturn(vacio);

        mockMvc.perform(delete("/api/v1/carrito/items/7").header("X-Carrito-Id", ID))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/carrito").header("X-Carrito-Id", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Carrito vaciado."));
    }

    @Test
    void comprarOkResponde200() throws Exception {
        when(carritoService.comprar(ID)).thenReturn(new CompraResponseDTO(
                "Compra simulada completada. Stock actualizado.", List.of(), 1, 49990.0));

        mockMvc.perform(post("/api/v1/carrito/comprar").header("X-Carrito-Id", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Compra simulada completada. Stock actualizado."));
    }

    @Test
    void comprarConCarritoVacioResponde400() throws Exception {
        when(carritoService.comprar(ID)).thenThrow(new CarritoVacioException());

        mockMvc.perform(post("/api/v1/carrito/comprar").header("X-Carrito-Id", ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El carrito está vacío."));
    }

    @Test
    void comprarSinStockResponde409ConDetalles() throws Exception {
        when(carritoService.comprar(anyString())).thenThrow(new StockInsuficienteException(
                "Stock insuficiente para completar la compra.", List.of("Elden Ring: pediste 6, quedan 5")));

        mockMvc.perform(post("/api/v1/carrito/comprar").header("X-Carrito-Id", ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0]").value("Elden Ring: pediste 6, quedan 5"));
    }

    @Test
    void metodoNoPermitidoResponde405() throws Exception {
        mockMvc.perform(put("/api/v1/carrito/comprar").header("X-Carrito-Id", ID))
                .andExpect(status().isMethodNotAllowed());
    }
}