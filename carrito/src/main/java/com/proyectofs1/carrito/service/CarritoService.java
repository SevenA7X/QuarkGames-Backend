package com.proyectofs1.carrito.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectofs1.carrito.dto.CarritoResponseDTO;
import com.proyectofs1.carrito.dto.CompraResponseDTO;
import com.proyectofs1.carrito.dto.ItemCarritoDTO;
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

@Service
@Transactional
public class CarritoService {

    private static final Logger log = LoggerFactory.getLogger(CarritoService.class);

    // Mismos mensajes que el reducer, para que React los muestre igual que antes
    static final String MSG_AGREGADO = "Producto agregado.";
    static final String MSG_ACTUALIZADO = "Cantidad actualizada.";
    static final String MSG_QUITADO = "Producto eliminado del carrito.";
    static final String MSG_VACIADO = "Carrito vaciado.";
    static final String MSG_STOCK = "Stock insuficiente.";
    static final String MSG_STOCK_COMPRA = "Stock insuficiente para completar la compra.";
    static final String MSG_COMPRA_OK = "Compra simulada completada. Stock actualizado.";

    private final CarritoRepository carritoRepository;
    private final CatalogoService catalogoService;

    public CarritoService(CarritoRepository carritoRepository, CatalogoService catalogoService) {
        this.carritoRepository = carritoRepository;
        this.catalogoService = catalogoService;
    }

    // GET: si el carrito aún no existe, se devuelve vacío (no es un error)
    @Transactional(readOnly = true)
    public CarritoResponseDTO obtener(String carritoId) {
        validarId(carritoId);
        return carritoRepository.findById(carritoId)
                .map(c -> aRespuesta(c, null))
                .orElseGet(() -> carritoVacio(carritoId, null));
    }

    // AGREGAR_AL_CARRITO: no se puede superar el stock -> "Stock insuficiente."
    public CarritoResponseDTO agregar(String carritoId, Long juegoId, int cantidad) {
        validarId(carritoId);
        JuegoCatalogoDTO juego = catalogoService.obtenerJuego(juegoId);   // 404 si no existe

        Carrito carrito = carritoRepository.findById(carritoId).orElseGet(() -> new Carrito(carritoId));
        ItemCarrito item = carrito.buscarItem(juegoId).orElse(null);
        int cantidadActual = item == null ? 0 : item.getCantidad();

        // Regla de negocio del reducer: cantidad >= producto.stock -> Stock insuficiente
        if (cantidadActual + cantidad > stockDe(juego)) {
            log.warn("Stock insuficiente al agregar juego {}: en carrito {}, pide {}, stock {}",
                    juegoId, cantidadActual, cantidad, juego.getStock());
            throw new StockInsuficienteException(MSG_STOCK);
        }

        if (item == null) {
            carrito.agregarItem(new ItemCarrito(juegoId, juego.getTitulo(), juego.getImagen(),
                    juego.getPrecio(), cantidad));
        } else {
            item.setCantidad(cantidadActual + cantidad);
            actualizarFoto(item, juego);   // por si cambió el precio en el catálogo
        }
        carrito.marcarActualizado();
        return aRespuesta(carritoRepository.save(carrito), MSG_AGREGADO);
    }

    // PUT con cantidad exacta. RESTAR_DEL_CARRITO = cantidad actual - 1 (con 0 se elimina)
    public CarritoResponseDTO actualizarCantidad(String carritoId, Long juegoId, int cantidad) {
        validarId(carritoId);
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new ItemNoEncontradoException(juegoId));
        ItemCarrito item = carrito.buscarItem(juegoId)
                .orElseThrow(() -> new ItemNoEncontradoException(juegoId));

        if (cantidad == 0) {
            carrito.quitarItem(juegoId);
            carrito.marcarActualizado();
            return aRespuesta(carritoRepository.save(carrito), MSG_QUITADO);
        }

        // Solo hace falta consultar el stock si se AUMENTA la cantidad
        if (cantidad > item.getCantidad()) {
            JuegoCatalogoDTO juego = catalogoService.obtenerJuego(juegoId);
            if (cantidad > stockDe(juego)) {
                throw new StockInsuficienteException(MSG_STOCK);
            }
            actualizarFoto(item, juego);
        }
        item.setCantidad(cantidad);
        carrito.marcarActualizado();
        return aRespuesta(carritoRepository.save(carrito), MSG_ACTUALIZADO);
    }

    // QUITAR_DEL_CARRITO: idempotente (si no estaba, no pasa nada; igual que el filter() del reducer)
    public CarritoResponseDTO quitar(String carritoId, Long juegoId) {
        validarId(carritoId);
        return carritoRepository.findById(carritoId)
                .map(c -> {
                    c.quitarItem(juegoId);
                    c.marcarActualizado();
                    return aRespuesta(carritoRepository.save(c), MSG_QUITADO);
                })
                .orElseGet(() -> carritoVacio(carritoId, MSG_QUITADO));
    }

    // VACIAR_CARRITO
    public CarritoResponseDTO vaciar(String carritoId) {
        validarId(carritoId);
        carritoRepository.findById(carritoId).ifPresent(c -> {
            c.vaciar();
            c.marcarActualizado();
            carritoRepository.save(c);
        });
        return carritoVacio(carritoId, MSG_VACIADO);
    }

    // COMPRAR:
    //  1) el carrito no puede estar vacío
    //  2) TODOS los productos deben tener stock suficiente (si uno falla, no se modifica nada)
    //  3) se descuenta el stock en el catálogo y se vacía el carrito
    public CompraResponseDTO comprar(String carritoId) {
        validarId(carritoId);
        Carrito carrito = carritoRepository.findById(carritoId)
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(CarritoVacioException::new);

        // Regla 2: revisamos TODO antes de tocar nada, y juntamos todos los problemas
        List<String> problemas = new ArrayList<>();
        for (ItemCarrito item : carrito.getItems()) {
            try {
                JuegoCatalogoDTO juego = catalogoService.obtenerJuego(item.getJuegoId());
                if (item.getCantidad() > stockDe(juego)) {
                    problemas.add(item.getTitulo() + ": pediste " + item.getCantidad()
                            + ", quedan " + stockDe(juego));
                } else {
                    actualizarFoto(item, juego);   // cobramos el precio vigente
                }
            } catch (JuegoNoEncontradoException e) {
                // En el reducer: !producto -> problema de stock
                problemas.add(item.getTitulo() + ": ya no está disponible en el catálogo");
            }
        }
        if (!problemas.isEmpty()) {
            log.warn("Compra rechazada para carrito {}: {}", carritoId, problemas);
            throw new StockInsuficienteException(MSG_STOCK_COMPRA, problemas);
        }

        // Regla 3: el catálogo descuenta TODO en una sola transacción (o nada)
        List<ItemStockDTO> aDescontar = carrito.getItems().stream()
                .map(i -> new ItemStockDTO(i.getJuegoId(), i.getCantidad()))
                .toList();
        catalogoService.descontarStock(aDescontar);

        // Armamos el resumen ANTES de vaciar
        CarritoResponseDTO resumen = aRespuesta(carrito, null);
        carrito.vaciar();
        carrito.marcarActualizado();
        carritoRepository.save(carrito);
        log.info("Compra completada para carrito {}: {} unidades, total {}",
                carritoId, resumen.getTotalUnidades(), resumen.getTotal());

        return new CompraResponseDTO(MSG_COMPRA_OK, resumen.getItems(),
                resumen.getTotalUnidades(), resumen.getTotal());
    }

    // ===== Métodos auxiliares =====

    // El id lo genera React con crypto.randomUUID(). Validarlo evita guardar basura como clave primaria.
    private void validarId(String carritoId) {
        try {
            UUID.fromString(carritoId);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CarritoIdInvalidoException();
        }
        if (carritoId.length() != 36) {
            throw new CarritoIdInvalidoException();
        }
    }

    private int stockDe(JuegoCatalogoDTO juego) {
        return juego.getStock() == null ? 0 : juego.getStock();
    }

    private void actualizarFoto(ItemCarrito item, JuegoCatalogoDTO juego) {
        item.setTitulo(juego.getTitulo());
        item.setPrecioUnitario(juego.getPrecio());
        item.setImagen(juego.getImagen());
    }

    private CarritoResponseDTO aRespuesta(Carrito carrito, String mensaje) {
        List<ItemCarritoDTO> items = carrito.getItems().stream()
                .map(i -> new ItemCarritoDTO(i.getJuegoId(), i.getTitulo(), i.getImagen(),
                        i.getPrecioUnitario(), i.getCantidad(), i.subtotal()))
                .toList();
        int unidades = items.stream().mapToInt(ItemCarritoDTO::getCantidad).sum();
        double total = items.stream().mapToDouble(ItemCarritoDTO::getSubtotal).sum();
        return new CarritoResponseDTO(carrito.getId(), items, unidades, total, mensaje);
    }

    private CarritoResponseDTO carritoVacio(String carritoId, String mensaje) {
        return new CarritoResponseDTO(carritoId, List.of(), 0, 0.0, mensaje);
    }
}