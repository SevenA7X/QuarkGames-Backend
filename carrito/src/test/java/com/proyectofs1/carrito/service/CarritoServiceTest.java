package com.proyectofs1.carrito.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyectofs1.carrito.dto.CarritoResponseDTO;
import com.proyectofs1.carrito.dto.CompraResponseDTO;
import com.proyectofs1.carrito.dto.ItemStockDTO;
import com.proyectofs1.carrito.dto.JuegoCatalogoDTO;
import com.proyectofs1.carrito.exception.CarritoIdInvalidoException;
import com.proyectofs1.carrito.exception.CarritoVacioException;
import com.proyectofs1.carrito.exception.ItemNoEncontradoException;
import com.proyectofs1.carrito.exception.JuegoNoEncontradoException;
import com.proyectofs1.carrito.exception.StockInsuficienteException;
import com.proyectofs1.carrito.model.Carrito;
import com.proyectofs1.carrito.model.ItemCarrito;
import com.proyectofs1.carrito.repository.CarritoRepository;

// Prueba UNITARIA de la lógica portada desde tiendaReducer.js.
// El repositorio y el catálogo son mocks: no hay base de datos ni HTTP.
@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    private static final String ID = "3f2b8c1e-6a4d-4e2b-9c7a-1d2e3f4a5b6c";

    @Mock private CarritoRepository carritoRepository;
    @Mock private CatalogoService catalogoService;

    @InjectMocks private CarritoService carritoService;

    private final JuegoCatalogoDTO elden = new JuegoCatalogoDTO(7L, "Elden Ring", 49990.0, 5, "https://img/elden.jpg");
    private final JuegoCatalogoDTO doom = new JuegoCatalogoDTO(3L, "DOOM Eternal", 35000.0, 30, "https://img/doom.jpg");

    // save() devuelve el mismo carrito que recibe (como haría JPA)
    private void saveDevuelveLoMismo() {
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Carrito carritoCon(ItemCarrito... items) {
        Carrito c = new Carrito(ID);
        for (ItemCarrito i : items) {
            c.agregarItem(i);
        }
        return c;
    }

    @Nested
    @DisplayName("AGREGAR_AL_CARRITO")
    class Agregar {

        @Test
        void agregaUnJuegoNuevoConLaFotoDelCatalogo() {
            when(catalogoService.obtenerJuego(7L)).thenReturn(elden);
            when(carritoRepository.findById(ID)).thenReturn(Optional.empty());
            saveDevuelveLoMismo();

            CarritoResponseDTO r = carritoService.agregar(ID, 7L, 1);

            assertThat(r.getMensaje()).isEqualTo("Producto agregado.");
            assertThat(r.getItems()).hasSize(1);
            assertThat(r.getItems().get(0).getTitulo()).isEqualTo("Elden Ring");
            assertThat(r.getItems().get(0).getCantidad()).isEqualTo(1);
            assertThat(r.getTotal()).isEqualTo(49990.0);
        }

        @Test
        void siElJuegoYaEstaSumaLaCantidad() {
            when(catalogoService.obtenerJuego(7L)).thenReturn(elden);
            when(carritoRepository.findById(ID))
                    .thenReturn(Optional.of(carritoCon(new ItemCarrito(7L, "Elden Ring", null, 49990.0, 2))));
            saveDevuelveLoMismo();

            CarritoResponseDTO r = carritoService.agregar(ID, 7L, 1);

            assertThat(r.getItems()).hasSize(1);
            assertThat(r.getItems().get(0).getCantidad()).isEqualTo(3);
            assertThat(r.getTotalUnidades()).isEqualTo(3);
        }

        @Test
        void noPermiteSuperarElStock() {
            // Stock 5 y ya hay 5 en el carrito: igual que el test del reducer
            when(catalogoService.obtenerJuego(7L)).thenReturn(elden);
            when(carritoRepository.findById(ID))
                    .thenReturn(Optional.of(carritoCon(new ItemCarrito(7L, "Elden Ring", null, 49990.0, 5))));

            assertThatThrownBy(() -> carritoService.agregar(ID, 7L, 1))
                    .isInstanceOf(StockInsuficienteException.class)
                    .hasMessage("Stock insuficiente.");
            verify(carritoRepository, never()).save(any());
        }

        @Test
        void juegoInexistenteLanza404YNoGuarda() {
            when(catalogoService.obtenerJuego(99L)).thenThrow(new JuegoNoEncontradoException(99L));

            assertThatThrownBy(() -> carritoService.agregar(ID, 99L, 1))
                    .isInstanceOf(JuegoNoEncontradoException.class);
            verify(carritoRepository, never()).save(any());
        }

        @Test
        void idQueNoEsUuidSeRechazaSinLlamarAlCatalogo() {
            assertThatThrownBy(() -> carritoService.agregar("hola", 7L, 1))
                    .isInstanceOf(CarritoIdInvalidoException.class);
            verify(catalogoService, never()).obtenerJuego(anyLong());
        }
    }

    @Nested
    @DisplayName("RESTAR / cambiar cantidad")
    class ActualizarCantidad {

        @Test
        void restarHastaCeroQuitaElItem() {
            when(carritoRepository.findById(ID))
                    .thenReturn(Optional.of(carritoCon(new ItemCarrito(7L, "Elden Ring", null, 49990.0, 1))));
            saveDevuelveLoMismo();

            CarritoResponseDTO r = carritoService.actualizarCantidad(ID, 7L, 0);

            assertThat(r.getItems()).isEmpty();
            assertThat(r.getTotal()).isZero();
        }

        @Test
        void restarNoConsultaElCatalogo() {
            when(carritoRepository.findById(ID))
                    .thenReturn(Optional.of(carritoCon(new ItemCarrito(7L, "Elden Ring", null, 49990.0, 3))));
            saveDevuelveLoMismo();

            CarritoResponseDTO r = carritoService.actualizarCantidad(ID, 7L, 2);

            assertThat(r.getItems().get(0).getCantidad()).isEqualTo(2);
            verify(catalogoService, never()).obtenerJuego(anyLong());
        }

        @Test
        void aumentarPorSobreElStockLanza409() {
            when(carritoRepository.findById(ID))
                    .thenReturn(Optional.of(carritoCon(new ItemCarrito(7L, "Elden Ring", null, 49990.0, 1))));
            when(catalogoService.obtenerJuego(7L)).thenReturn(elden);   // stock 5

            assertThatThrownBy(() -> carritoService.actualizarCantidad(ID, 7L, 6))
                    .isInstanceOf(StockInsuficienteException.class);
        }

        @Test
        void juegoQueNoEstaEnElCarritoLanza404() {
            when(carritoRepository.findById(ID)).thenReturn(Optional.of(carritoCon()));

            assertThatThrownBy(() -> carritoService.actualizarCantidad(ID, 7L, 2))
                    .isInstanceOf(ItemNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("QUITAR, VACIAR y ver carrito")
    class QuitarVaciarObtener {

        @Test
        void quitarEliminaSoloEseJuego() {
            when(carritoRepository.findById(ID)).thenReturn(Optional.of(carritoCon(
                    new ItemCarrito(7L, "Elden Ring", null, 49990.0, 1),
                    new ItemCarrito(3L, "DOOM Eternal", null, 35000.0, 2))));
            saveDevuelveLoMismo();

            CarritoResponseDTO r = carritoService.quitar(ID, 7L);

            assertThat(r.getItems()).extracting("juegoId").containsExactly(3L);
            assertThat(r.getTotal()).isEqualTo(70000.0);
        }

        @Test
        void quitarEnCarritoInexistenteNoFalla() {
            when(carritoRepository.findById(ID)).thenReturn(Optional.empty());

            CarritoResponseDTO r = carritoService.quitar(ID, 7L);

            assertThat(r.getItems()).isEmpty();
        }

        @Test
        void vaciarDejaElCarritoSinItems() {
            Carrito carrito = carritoCon(new ItemCarrito(7L, "Elden Ring", null, 49990.0, 2));
            when(carritoRepository.findById(ID)).thenReturn(Optional.of(carrito));

            CarritoResponseDTO r = carritoService.vaciar(ID);

            assertThat(r.getItems()).isEmpty();
            assertThat(carrito.getItems()).isEmpty();
            verify(carritoRepository).save(carrito);
        }

        @Test
        void obtenerUnCarritoQueNoExisteDevuelveVacio() {
            when(carritoRepository.findById(ID)).thenReturn(Optional.empty());

            CarritoResponseDTO r = carritoService.obtener(ID);

            assertThat(r.getCarritoId()).isEqualTo(ID);
            assertThat(r.getItems()).isEmpty();
            assertThat(r.getTotal()).isZero();
        }
    }

    @Nested
    @DisplayName("COMPRAR")
    class Comprar {

        @Test
        void conCarritoVacioLanzaExcepcion() {
            when(carritoRepository.findById(ID)).thenReturn(Optional.of(carritoCon()));

            assertThatThrownBy(() -> carritoService.comprar(ID))
                    .isInstanceOf(CarritoVacioException.class)
                    .hasMessage("El carrito está vacío.");
        }

        @Test
        void siUnJuegoNoTieneStockNoSeModificaNada() {
            Carrito carrito = carritoCon(
                    new ItemCarrito(3L, "DOOM Eternal", null, 35000.0, 1),
                    new ItemCarrito(7L, "Elden Ring", null, 49990.0, 6));   // stock 5
            when(carritoRepository.findById(ID)).thenReturn(Optional.of(carrito));
            when(catalogoService.obtenerJuego(3L)).thenReturn(doom);
            when(catalogoService.obtenerJuego(7L)).thenReturn(elden);

            assertThatThrownBy(() -> carritoService.comprar(ID))
                    .isInstanceOf(StockInsuficienteException.class)
                    .hasMessage("Stock insuficiente para completar la compra.")
                    .extracting("detalles").asList()
                    .containsExactly("Elden Ring: pediste 6, quedan 5");

            // Igual que en el reducer: catálogo y carrito quedan como estaban
            verify(catalogoService, never()).descontarStock(anyList());
            verify(carritoRepository, never()).save(any());
            assertThat(carrito.getItems()).hasSize(2);
        }

        @Test
        void siUnJuegoFueEliminadoDelCatalogoSeRechaza() {
            when(carritoRepository.findById(ID)).thenReturn(Optional.of(
                    carritoCon(new ItemCarrito(7L, "Elden Ring", null, 49990.0, 1))));
            when(catalogoService.obtenerJuego(7L)).thenThrow(new JuegoNoEncontradoException(7L));

            assertThatThrownBy(() -> carritoService.comprar(ID))
                    .isInstanceOf(StockInsuficienteException.class);
            verify(catalogoService, never()).descontarStock(anyList());
        }

        @Test
        void compraExitosaDescuentaStockYVaciaElCarrito() {
            Carrito carrito = carritoCon(
                    new ItemCarrito(7L, "Elden Ring", null, 49990.0, 2),
                    new ItemCarrito(3L, "DOOM Eternal", null, 35000.0, 1));
            when(carritoRepository.findById(ID)).thenReturn(Optional.of(carrito));
            when(catalogoService.obtenerJuego(7L)).thenReturn(elden);
            when(catalogoService.obtenerJuego(3L)).thenReturn(doom);

            CompraResponseDTO r = carritoService.comprar(ID);

            assertThat(r.getMensaje()).isEqualTo("Compra simulada completada. Stock actualizado.");
            assertThat(r.getTotal()).isEqualTo(2 * 49990.0 + 35000.0);
            assertThat(r.getTotalUnidades()).isEqualTo(3);

            // Se envía al catálogo exactamente lo que había en el carrito, en UNA sola llamada
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<ItemStockDTO>> captor = ArgumentCaptor.forClass(List.class);
            verify(catalogoService).descontarStock(captor.capture());
            assertThat(captor.getValue())
                    .containsExactly(new ItemStockDTO(7L, 2), new ItemStockDTO(3L, 1));

            assertThat(carrito.getItems()).isEmpty();
            verify(carritoRepository).save(carrito);
        }

        @Test
        void siElCatalogoRechazaElDescuentoElCarritoNoSeVacia() {
            Carrito carrito = carritoCon(new ItemCarrito(7L, "Elden Ring", null, 49990.0, 1));
            when(carritoRepository.findById(ID)).thenReturn(Optional.of(carrito));
            when(catalogoService.obtenerJuego(7L)).thenReturn(elden);
            // Otro cliente compró justo antes: el catálogo responde 409
            org.mockito.Mockito.doThrow(new StockInsuficienteException("Stock insuficiente para completar la compra."))
                    .when(catalogoService).descontarStock(anyList());

            assertThatThrownBy(() -> carritoService.comprar(ID))
                    .isInstanceOf(StockInsuficienteException.class);
            assertThat(carrito.getItems()).hasSize(1);
            verify(carritoRepository, never()).save(any());
        }
    }
}