package com.proyectofs1.catalogo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyectofs1.catalogo.dto.ItemStockDTO;
import com.proyectofs1.catalogo.exception.JuegoNoEncontradoException;
import com.proyectofs1.catalogo.exception.StockInsuficienteException;
import com.proyectofs1.catalogo.model.Juego;
import com.proyectofs1.catalogo.repository.JuegoRepository;

// Pruebas del descuento de stock que usa el MS Carrito (mismo estilo GIVEN/WHEN/THEN que JuegoServiceTest)
@ExtendWith(MockitoExtension.class)
class JuegoServiceDescontarStockTest {

    @Mock
    private JuegoRepository juegoRepository;

    @InjectMocks
    private JuegoService juegoService;

    private Juego juego(Long id, String titulo, int stock) {
        Juego j = new Juego();
        j.setId(id);
        j.setTitulo(titulo);
        j.setStock(stock);
        return j;
    }

    @Test
    void descontarStock_DeberiaDescontarTodos_CuandoHayStock() {
        // --- GIVEN ---
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego(1L, "Hogwarts Legacy", 15)));
        when(juegoRepository.findById(7L)).thenReturn(Optional.of(juego(7L, "Elden Ring", 5)));
        when(juegoRepository.descontarStock(anyLong(), anyInt())).thenReturn(1);

        // --- WHEN ---
        juegoService.descontarStock(List.of(new ItemStockDTO(1L, 2), new ItemStockDTO(7L, 5)));

        // --- THEN ---
        verify(juegoRepository).descontarStock(1L, 2);
        verify(juegoRepository).descontarStock(7L, 5);
    }

    @Test
    void descontarStock_NoDeberiaDescontarNada_CuandoUnJuegoNoTieneStock() {
        // --- GIVEN --- el segundo juego no alcanza
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego(1L, "Hogwarts Legacy", 15)));
        when(juegoRepository.findById(7L)).thenReturn(Optional.of(juego(7L, "Elden Ring", 5)));

        // --- WHEN / THEN ---
        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> juegoService.descontarStock(List.of(new ItemStockDTO(1L, 2), new ItemStockDTO(7L, 6))));
        assertTrue(ex.getMessage().contains("Elden Ring"));

        // Se validó todo ANTES de descontar: no se tocó ningún stock
        verify(juegoRepository, never()).descontarStock(anyLong(), anyInt());
    }

    @Test
    void descontarStock_DeberiaLanzar404_CuandoElJuegoNoExiste() {
        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(JuegoNoEncontradoException.class,
                () -> juegoService.descontarStock(List.of(new ItemStockDTO(99L, 1))));
        verify(juegoRepository, never()).descontarStock(anyLong(), anyInt());
    }

    @Test
    void descontarStock_DeberiaFallar_CuandoOtraCompraSeAdelanto() {
        // --- GIVEN --- al validar había 5, pero el UPDATE no modificó filas (otra compra se adelantó)
        when(juegoRepository.findById(7L))
                .thenReturn(Optional.of(juego(7L, "Elden Ring", 5)))
                .thenReturn(Optional.of(juego(7L, "Elden Ring", 0)));
        when(juegoRepository.descontarStock(7L, 5)).thenReturn(0);

        // --- WHEN / THEN --- la excepción provoca rollback de la transacción
        assertThrows(StockInsuficienteException.class,
                () -> juegoService.descontarStock(List.of(new ItemStockDTO(7L, 5))));
    }
}
