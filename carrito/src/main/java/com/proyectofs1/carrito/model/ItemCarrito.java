package com.proyectofs1.carrito.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una línea del carrito: qué juego y cuántas unidades.
// Guardamos una "foto" (snapshot) del título, precio e imagen al momento de agregar,
// para poder mostrar el carrito y calcular el total sin llamar al catálogo cada vez.
// El stock NO se guarda aquí: siempre se consulta en vivo al catálogo.
@Entity
@Table(name = "item_carrito",
       uniqueConstraints = @UniqueConstraint(columnNames = {"carrito_id", "juego_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    // Id del juego en el MS Catálogo (otra base de datos: no puede ser una FK real)
    @Column(name = "juego_id", nullable = false)
    private Long juegoId;

    @Column(nullable = false, length = 50)
    private String titulo;

    @Column(length = 500)
    private String imagen;

    @Column(nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    public ItemCarrito(Long juegoId, String titulo, String imagen, Double precioUnitario, Integer cantidad) {
        this.juegoId = juegoId;
        this.titulo = titulo;
        this.imagen = imagen;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public double subtotal() {
        return precioUnitario * cantidad;
    }
}
