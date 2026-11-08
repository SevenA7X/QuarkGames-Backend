package com.proyectofs1.carrito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectofs1.carrito.model.Carrito;

// La clave primaria del carrito es un String (el UUID de X-Carrito-Id).
// No necesitamos repositorio de ItemCarrito: los ítems se guardan en cascada con su carrito.
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, String> {
}
