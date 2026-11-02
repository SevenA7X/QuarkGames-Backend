package com.proyectofs1.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyectofs1.catalogo.model.Juego;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long> {
    
    // 1. Búsqueda derivada corregida: Ahora navega hasta el atributo "nombre" de la entidad "Categoria"
    List<Juego> findByCategoriaNombre(String nombre);

    // 2. Búsqueda por coincidencia parcial en el título (Se mantiene igual)
    List<Juego> findByTituloContainingIgnoreCase(String titulo);

    // 3. Búsqueda por precio menor o igual (Se mantiene igual)
    List<Juego> findByPrecioLessThanEqual(double precio);

    // 4. Consulta JPQL corregida: Se cambia "j.genero" por "j.categoria.nombre"
    @Query("SELECT j FROM Juego j WHERE j.categoria.nombre = :genero AND j.precio <= :precioMaximo")
    List<Juego> buscarPorGeneroYPrecioMaximo(@Param("genero") String genero, @Param("precioMaximo") double precioMaximo);

    
}