package com.proyectofs1.catalogo.dto;

import org.springframework.hateoas.RepresentationModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class JuegoDTO extends RepresentationModel<JuegoDTO> {
    
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 50, message = "El título no puede exceder los 50 caracteres")
    private String titulo;

    @NotBlank(message = "La Plataforma no puede estar vacía")
    @Size(max = 30, message = "La plataforma no puede exceder los 30 caracteres")
    private String plataforma;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio debe ser un valor positivo")
    private Double precio;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 250, message = "La descripción no puede exceder los 250 caracteres")
    private String descripcion;

    // ID de la categoría (utilizado al crear o actualizar el juego desde React)
    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoriaId;

    // Objeto anidado de la categoría (utilizado para enviar el nombre de vuelta a React)
    private CategoriaDTO categoria;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @NotBlank(message = "La imagen no puede estar vacía")
    private String imagen;
}