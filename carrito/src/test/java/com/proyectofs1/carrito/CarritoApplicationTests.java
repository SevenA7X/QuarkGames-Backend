package com.proyectofs1.carrito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.proyectofs1.carrito.client.CatalogoClient;
import com.proyectofs1.carrito.dto.JuegoCatalogoDTO;
import com.proyectofs1.carrito.exception.StockInsuficienteException;
import com.proyectofs1.carrito.repository.CarritoRepository;
import com.proyectofs1.carrito.service.CarritoService;

// Prueba de INTEGRACIÓN: Spring completo + JPA + H2 reales.
// Solo el cliente Feign es mock (no hay un catálogo corriendo en las pruebas).
@SpringBootTest
class CarritoApplicationTests {

    private static final String ID = "aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee";

    @Autowired private CarritoService carritoService;
    @Autowired private CarritoRepository carritoRepository;

    @MockBean private CatalogoClient catalogoClient;

    @Test
    void flujoCompletoAgregarRestarYComprarPersisteEnLaBaseDeDatos() {
        when(catalogoClient.obtenerJuego(7L))
                .thenReturn(new JuegoCatalogoDTO(7L, "Elden Ring", 49990.0, 5, "https://img"));

        carritoService.agregar(ID, 7L, 1);
        carritoService.agregar(ID, 7L, 2);
        carritoService.actualizarCantidad(ID, 7L, 2);   // "restar" 1

        // Se guardó de verdad en H2
        assertThat(carritoService.obtener(ID).getTotalUnidades()).isEqualTo(2);
        assertThat(carritoRepository.findById(ID)).isPresent();

        var compra = carritoService.comprar(ID);

        assertThat(compra.getTotal()).isEqualTo(99980.0);
        // La clave interna se leyó de src/test/resources/application.properties y viajó en la petición
        verify(catalogoClient).descontarStock(eq("clave-de-prueba"), any());
        assertThat(carritoService.obtener(ID).getItems()).isEmpty();
    }

    @Test
    void noSePuedeSuperarElStockDelCatalogo() {
        String otro = "11111111-2222-4333-8444-555555555555";
        when(catalogoClient.obtenerJuego(3L))
                .thenReturn(new JuegoCatalogoDTO(3L, "DOOM Eternal", 35000.0, 1, null));

        carritoService.agregar(otro, 3L, 1);

        assertThatThrownBy(() -> carritoService.agregar(otro, 3L, 1))
                .isInstanceOf(StockInsuficienteException.class);
        assertThat(carritoService.obtener(otro).getTotalUnidades()).isEqualTo(1);
    }
}