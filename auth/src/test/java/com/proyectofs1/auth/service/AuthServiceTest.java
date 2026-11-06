package com.proyectofs1.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyectofs1.auth.dto.LoginRequestDTO;
import com.proyectofs1.auth.dto.LoginResponseDTO;
import com.proyectofs1.auth.exception.CredencialesInvalidasException;
import com.proyectofs1.auth.model.Administrador;
import com.proyectofs1.auth.repository.AdministradorRepository;
import com.proyectofs1.auth.security.JwtService;

// Prueba UNITARIA: no levanta Spring ni base de datos.
// Las dependencias del servicio son "mocks" (dobles) que programamos con when(...).
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AdministradorRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private final Administrador admin = new Administrador(1L, "admin", "$2a$10$hashFalso", "ADMIN");

    @Test
    @DisplayName("Login correcto devuelve token, usuario y rol")
    void loginCorrecto() {
        // 1 Preparar
        when(repository.findByUsuario("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("1234", "$2a$10$hashFalso")).thenReturn(true);
        when(jwtService.generarToken("admin", "ADMIN")).thenReturn("token.jwt.falso");
        when(jwtService.getExpiracionMs()).thenReturn(3600000L);

        // 2 Actuar (con espacios: deben ignorarse, igual que en React)
        LoginResponseDTO respuesta = authService.login(new LoginRequestDTO("  admin ", "1234"));

        // 3 Comprobar
        assertThat(respuesta.getToken()).isEqualTo("token.jwt.falso");
        assertThat(respuesta.getTipo()).isEqualTo("Bearer");
        assertThat(respuesta.getUsuario()).isEqualTo("admin");
        assertThat(respuesta.getRol()).isEqualTo("ADMIN");
        assertThat(respuesta.getExpiraEnMs()).isEqualTo(3600000L);
    }

    @Test
    @DisplayName("Usuario inexistente lanza CredencialesInvalidasException y no genera token")
    void usuarioInexistente() {
        when(repository.findByUsuario("otro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("otro", "1234")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Usuario o contraseña incorrectos.");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generarToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Clave incorrecta lanza CredencialesInvalidasException y no genera token")
    void claveIncorrecta() {
        when(repository.findByUsuario("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("9999", "$2a$10$hashFalso")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("admin", "9999")))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(jwtService, never()).generarToken(anyString(), anyString());
    }
}