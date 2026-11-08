package com.proyectofs1.catalogo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyectofs1.catalogo.dto.DescontarStockDTO;
import com.proyectofs1.catalogo.dto.ItemStockDTO;
import com.proyectofs1.catalogo.exception.AccesoInternoDenegadoException;
import com.proyectofs1.catalogo.service.JuegoService;

// Verifica la clave interna del endpoint que usa el MS Carrito
@ExtendWith(MockitoExtension.class)
class InternalStockControllerTest {

    @Mock
    private JuegoService juegoService;

    private InternalStockController controller;

    private final DescontarStockDTO solicitud =
            new DescontarStockDTO(List.of(new ItemStockDTO(1L, 2)));

    @BeforeEach
    void crear() {
        controller = new InternalStockController(juegoService, "clave-correcta");
    }

    @Test
    void descontarStock_DeberiaResponder204_CuandoLaClaveEsCorrecta() {
        // --- WHEN ---
        var respuesta = controller.descontarStock("clave-correcta", solicitud);

        // --- THEN ---
        assertEquals(204, respuesta.getStatusCode().value());
        verify(juegoService, times(1)).descontarStock(solicitud.getItems());
    }

    @Test
    void descontarStock_DeberiaRechazar_CuandoLaClaveEsIncorrecta() {
        assertThrows(AccesoInternoDenegadoException.class,
                () -> controller.descontarStock("otra-clave", solicitud));
        verify(juegoService, never()).descontarStock(anyList());
    }

    @Test
    void descontarStock_DeberiaRechazar_CuandoNoVieneLaClave() {
        // Sin cabecera, @RequestHeader usa defaultValue = ""
        assertThrows(AccesoInternoDenegadoException.class,
                () -> controller.descontarStock("", solicitud));
        verify(juegoService, never()).descontarStock(anyList());
    }
}
