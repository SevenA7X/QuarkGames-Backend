package com.proyectofs1.catalogo.dto;
import org.springframework.hateoas.RepresentationModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JuegoDTO extends RepresentationModel<JuegoDTO> {
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 50, message = "El título no puede exceder los 50 caracteres")
    private String titulo;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio debe ser un valor positivo")
    private Double precio;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 250, message = "La descripción no puede exceder los 250 caracteres")
    private String descripcion;

    // Se recomienda usar el ID para vincular en el DTO de creación
    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoriaId;
}