package com.proyectofs1.carrito.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.proyectofs1.carrito.client.CatalogoClient;
import com.proyectofs1.carrito.dto.DescontarStockRequestDTO;
import com.proyectofs1.carrito.dto.ItemStockDTO;
import com.proyectofs1.carrito.dto.JuegoCatalogoDTO;
import com.proyectofs1.carrito.exception.CatalogoNoDisponibleException;
import com.proyectofs1.carrito.exception.JuegoNoEncontradoException;
import com.proyectofs1.carrito.exception.StockInsuficienteException;

import feign.FeignException;

// Envuelve al cliente Feign y TRADUCE sus errores HTTP a excepciones del carrito.
// Así CarritoService no necesita saber nada de Feign ni de códigos HTTP.
@Service
public class CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoService.class);

    private final CatalogoClient catalogoClient;
    private final String claveInterna;

    public CatalogoService(CatalogoClient catalogoClient,
                           @Value("${catalogo.internal-key}") String claveInterna) {
        this.catalogoClient = catalogoClient;
        this.claveInterna = claveInterna;
    }

    public JuegoCatalogoDTO obtenerJuego(Long juegoId) {
        try {
            JuegoCatalogoDTO juego = catalogoClient.obtenerJuego(juegoId);
            if (juego == null) {
                throw new JuegoNoEncontradoException(juegoId);
            }
            return juego;
        } catch (FeignException.NotFound e) {
            // El catálogo respondió 404
            throw new JuegoNoEncontradoException(juegoId);
        } catch (FeignException e) {
            // Catálogo apagado (RetryableException), timeout o error 5xx
            log.error("Error al consultar el juego {} en el catálogo: {}", juegoId, e.getMessage());
            throw new CatalogoNoDisponibleException(e);
        }
    }

    public void descontarStock(List<ItemStockDTO> items) {
        try {
            catalogoClient.descontarStock(claveInterna, new DescontarStockRequestDTO(items));
        } catch (FeignException.Conflict | FeignException.NotFound e) {
            // 409: alguien compró justo antes y ya no alcanza. 404: el juego se eliminó.
            // El catálogo hace rollback: no se descontó NADA.
            throw new StockInsuficienteException("Stock insuficiente para completar la compra.");
        } catch (FeignException.Forbidden e) {
            // 403: la clave interna no coincide. Es un error de CONFIGURACIÓN, no del usuario.
            log.error("El catálogo rechazó la clave interna (X-Internal-Key). Revisa INTERNAL_KEY en ambos servicios.");
            throw new CatalogoNoDisponibleException(e);
        } catch (FeignException e) {
            log.error("Error al descontar stock en el catálogo: {}", e.getMessage());
            throw new CatalogoNoDisponibleException(e);
        }
    }
}