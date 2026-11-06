package com.proyectofs1.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectofs1.auth.model.Administrador;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    // Consulta derivada: SELECT * FROM administrador WHERE usuario = ?
    Optional<Administrador> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}