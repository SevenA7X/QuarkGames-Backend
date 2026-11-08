package com.proyectofs1.carrito.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un carrito por navegador. El id es el UUID que React envía en la cabecera X-Carrito-Id.
// Usamos @Getter/@Setter y NO @Data: en relaciones bidireccionales (Carrito <-> ItemCarrito)
// el toString()/hashCode() de @Data se llamarían entre sí sin fin (StackOverflowError).
@Entity
@Table(name = "carrito")
@Getter
@Setter
@NoArgsConstructor
public class Carrito {

    @Id
    @Column(length = 36)
    private String id;

    // cascade = ALL: guardar el carrito guarda sus ítems.
    // orphanRemoval = true: quitar un ítem de la lista lo BORRA de la base de datos.
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemCarrito> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime actualizadoEn;

    public Carrito(String id) {
        this.id = id;
        this.actualizadoEn = LocalDateTime.now();
    }

    public Optional<ItemCarrito> buscarItem(Long juegoId) {
        return items.stream().filter(i -> i.getJuegoId().equals(juegoId)).findFirst();
    }

    // Mantiene sincronizados los dos lados de la relación
    public void agregarItem(ItemCarrito item) {
        item.setCarrito(this);
        items.add(item);
    }

    public void quitarItem(Long juegoId) {
        items.removeIf(i -> i.getJuegoId().equals(juegoId));
    }

    public void vaciar() {
        items.clear();
    }

    public void marcarActualizado() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
