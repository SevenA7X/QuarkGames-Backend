package com.proyectofs1.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyectofs1.auth.dto.LoginRequestDTO;
import com.proyectofs1.auth.dto.LoginResponseDTO;
import com.proyectofs1.auth.exception.CredencialesInvalidasException;
import com.proyectofs1.auth.model.Administrador;
import com.proyectofs1.auth.repository.AdministradorRepository;
import com.proyectofs1.auth.security.JwtService;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // Inyección por constructor: facilita las pruebas con Mockito (@InjectMocks)
    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AdministradorRepository repository, PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO datos) {
        // Igual que el frontend: se ignoran espacios alrededor del usuario
        String usuario = datos.getUsuario().trim();
        log.info("Intento de inicio de sesión para el usuario '{}'", usuario);

        Administrador admin = repository.findByUsuario(usuario)
                .orElseThrow(() -> {
                    log.warn("Login fallido: el usuario '{}' no existe", usuario);
                    return new CredencialesInvalidasException();
                });

        // matches() aplica BCrypt a la clave recibida y la compara con el hash guardado
        if (!passwordEncoder.matches(datos.getClave(), admin.getClave())) {
            log.warn("Login fallido: clave incorrecta para '{}'", usuario);
            throw new CredencialesInvalidasException();
        }

        String token = jwtService.generarToken(admin.getUsuario(), admin.getRol());
        log.info("Login exitoso para '{}'", usuario);

        return new LoginResponseDTO(token, "Bearer", admin.getUsuario(), admin.getRol(),
                jwtService.getExpiracionMs());
    }
}