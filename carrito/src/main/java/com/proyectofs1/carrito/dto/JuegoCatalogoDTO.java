package com.proyectofs1.carrito.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que el carrito necesita del JuegoDTO del MS Catálogo.
// ignoreUnknown = true: el catálogo envía más campos (descripcion, plataforma,
// categoria, _links de HATEOAS...) y simplemente se ignoran.
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JuegoCatalogoDTO {
    private Long id;
    private String titulo;
    private Double precio;
    private Integer stock;
    private String imagen;
}