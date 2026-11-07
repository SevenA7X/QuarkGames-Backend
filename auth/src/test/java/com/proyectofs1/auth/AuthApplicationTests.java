package com.proyectofs1.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyectofs1.auth.dto.LoginRequestDTO;
import com.proyectofs1.auth.model.Administrador;
import com.proyectofs1.auth.repository.AdministradorRepository;
import com.proyectofs1.auth.service.AuthService;

// Prueba de INTEGRACIÓN: levanta Spring completo con H2 (src/test/resources/application.properties).
// Comprueba que DataLoader + BCrypt + AuthService + JwtService funcionan juntos de verdad.
@SpringBootTest
class AuthApplicationTests {

    @Autowired private AdministradorRepository repository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthService authService;

    @Test
    void elAdminInicialQuedaGuardadoConBCrypt() {
        Administrador admin = repository.findByUsuario("admin").orElseThrow();

        assertThat(admin.getClave()).startsWith("$2a$").isNotEqualTo("1234");
        assertThat(passwordEncoder.matches("1234", admin.getClave())).isTrue();
    }

    @Test
    void loginRealDevuelveUnToken() {
        var respuesta = authService.login(new LoginRequestDTO("admin", "1234"));

        assertThat(respuesta.getToken()).isNotBlank();
    }
}