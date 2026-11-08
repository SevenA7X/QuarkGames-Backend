package com.proyectofs1.carrito.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyectofs1.carrito.client.CatalogoClient;
import com.proyectofs1.carrito.dto.DescontarStockRequestDTO;
import com.proyectofs1.carrito.dto.ItemStockDTO;
import com.proyectofs1.carrito.dto.JuegoCatalogoDTO;
import com.proyectofs1.carrito.exception.CatalogoNoDisponibleException;
import com.proyectofs1.carrito.exception.JuegoNoEncontradoException;
import com.proyectofs1.carrito.exception.StockInsuficienteException;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;

// Verifica que los errores HTTP del catálogo se traducen a excepciones del carrito.
// El cliente Feign es un mock: simulamos las respuestas 404, 409, 403 y "catálogo apagado".
@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    private static final String CLAVE = "clave-de-prueba";

    @Mock private CatalogoClient catalogoClient;

    // No usamos @InjectMocks: el constructor también recibe la clave (un String),
    // y Mockito no sabe qué String inyectar. Lo creamos a mano.
    private CatalogoService catalogoService;

    private static final Request PETICION = Request.create(Request.HttpMethod.GET,
            "http://localhost:8081/api/v1/juegos/7", Map.of(), null, StandardCharsets.UTF_8, null);

    @BeforeEach
    void crearServicio() {
        catalogoService = new CatalogoService(catalogoClient, CLAVE);
    }

    @Test
    void devuelveElJuegoCuandoExiste() {
        JuegoCatalogoDTO juego = new JuegoCatalogoDTO(7L, "Elden Ring", 49990.0, 5, null);
        when(catalogoClient.obtenerJuego(7L)).thenReturn(juego);

        assertThat(catalogoService.obtenerJuego(7L)).isSameAs(juego);
    }

    @Test
    void un404DelCatalogoSeConvierteEnJuegoNoEncontrado() {
        when(catalogoClient.obtenerJuego(99L))
                .thenThrow(new FeignException.NotFound("Not Found", PETICION, null, null));

        assertThatThrownBy(() -> catalogoService.obtenerJuego(99L))
                .isInstanceOf(JuegoNoEncontradoException.class);
    }

    @Test
    void catalogoApagadoSeConvierteEn503() {
        when(catalogoClient.obtenerJuego(7L)).thenThrow(new RetryableException(
                -1, "Connection refused", Request.HttpMethod.GET, (Long) null, PETICION));

        assertThatThrownBy(() -> catalogoService.obtenerJuego(7L))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }

    @Test
    void respuestaVaciaSeTrataComoJuegoNoEncontrado() {
        when(catalogoClient.obtenerJuego(7L)).thenReturn(null);

        assertThatThrownBy(() -> catalogoService.obtenerJuego(7L))
                .isInstanceOf(JuegoNoEncontradoException.class);
    }

    @Test
    void descontarEnviaLaClaveInternaYTodosLosItemsEnUnaSolaPeticion() {
        List<ItemStockDTO> items = List.of(new ItemStockDTO(7L, 2), new ItemStockDTO(3L, 1));

        catalogoService.descontarStock(items);

        verify(catalogoClient).descontarStock(CLAVE, new DescontarStockRequestDTO(items));
    }

    @Test
    void un409AlDescontarSeConvierteEnStockInsuficiente() {
        doThrow(new FeignException.Conflict("Conflict", PETICION, null, null))
                .when(catalogoClient).descontarStock(anyString(), any());

        assertThatThrownBy(() -> catalogoService.descontarStock(List.of(new ItemStockDTO(7L, 9))))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessage("Stock insuficiente para completar la compra.");
    }

    @Test
    void claveInternaIncorrectaSeConvierteEn503() {
        doThrow(new FeignException.Forbidden("Forbidden", PETICION, null, null))
                .when(catalogoClient).descontarStock(anyString(), any());

        assertThatThrownBy(() -> catalogoService.descontarStock(List.of(new ItemStockDTO(7L, 1))))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }

    @Test
    void un500AlDescontarSeConvierteEn503() {
        doThrow(new FeignException.InternalServerError("Error", PETICION, null, null))
                .when(catalogoClient).descontarStock(anyString(), any());

        assertThatThrownBy(() -> catalogoService.descontarStock(List.of(new ItemStockDTO(7L, 1))))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }
}
