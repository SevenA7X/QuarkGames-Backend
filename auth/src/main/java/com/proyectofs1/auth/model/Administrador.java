package com.proyectofs1.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "administrador")
@NoArgsConstructor
@AllArgsConstructor
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    // Hash BCrypt (siempre 60 caracteres). NUNCA se guarda la clave en texto plano.
    @Column(nullable = false, length = 60)
    private String clave;

    // Rol del usuario. Por ahora solo existe "ADMIN", pero deja abierta la puerta a otros.
    @Column(nullable = false, length = 20)
    private String rol;
}