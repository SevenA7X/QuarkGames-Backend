package com.proyectofs1.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuerpo que envía React: { "usuario": "admin", "clave": "1234" }
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @Schema(example = "admin")
    @NotBlank(message = "Ingresa tu usuario.")
    @Size(max = 50, message = "El usuario no puede exceder los 50 caracteres.")
    private String usuario;

    @Schema(example = "1234")
    @NotBlank(message = "Ingresa tu contraseña.")
    @Size(max = 72, message = "La contraseña no puede exceder los 72 caracteres.")
    private String clave;
}