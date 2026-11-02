package com.proyectofs1.catalogo.dto;

import org.springframework.hateoas.RepresentationModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoriaDTO extends RepresentationModel<CategoriaDTO> {
    private Long id;

    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;
}