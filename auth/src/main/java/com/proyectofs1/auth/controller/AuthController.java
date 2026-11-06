package com.proyectofs1.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyectofs1.auth.config.SwaggerConfig;
import com.proyectofs1.auth.dto.ErrorDTO;
import com.proyectofs1.auth.dto.LoginRequestDTO;
import com.proyectofs1.auth.dto.LoginResponseDTO;
import com.proyectofs1.auth.dto.SesionDTO;
import com.proyectofs1.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login de administradores con JWT.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida usuario y clave (BCrypt) y devuelve un JWT.")
    @ApiResponse(responseCode = "200", description = "Login correcto")
    @ApiResponse(responseCode = "400", description = "Campos vacíos o inválidos",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO datos) {
        return ResponseEntity.ok(authService.login(datos));
    }

    // Ruta protegida: solo responde 200 si el token es válido.
    // React la puede usar al recargar la página para saber si el token guardado sigue vigente.
    @GetMapping("/validar")
    @Operation(summary = "Validar token", description = "Devuelve el usuario y rol del token enviado.")
    @SecurityRequirement(name = SwaggerConfig.ESQUEMA_JWT)
    @ApiResponse(responseCode = "200", description = "Token válido")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<SesionDTO> validar(Authentication autenticacion) {
        // El JwtAuthFilter ya validó el token y dejó aquí el usuario y su rol
        String rol = autenticacion.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(new SesionDTO(autenticacion.getName(), rol));
    }
}