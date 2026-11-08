package com.proyectofs1.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.proyectofs1.auth.model.Administrador;
import com.proyectofs1.auth.repository.AdministradorRepository;

// Se ejecuta una vez al arrancar. Crea el admin inicial con la clave encriptada.
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String usuarioAdmin;
    private final String claveAdmin;

    public DataLoader(AdministradorRepository repository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.admin.usuario}") String usuarioAdmin,
                      @Value("${app.admin.clave}") String claveAdmin) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioAdmin = usuarioAdmin;
        this.claveAdmin = claveAdmin;
    }

    @Override
    public void run(String... args) {
        if (repository.existsByUsuario(usuarioAdmin)) {
            log.info("El administrador '{}' ya existe. No se crea de nuevo.", usuarioAdmin);
            return;
        }
        Administrador admin = new Administrador(null, usuarioAdmin,
                passwordEncoder.encode(claveAdmin), "ADMIN");
        repository.save(admin);
        log.info("Administrador inicial '{}' creado con clave encriptada (BCrypt).", usuarioAdmin);
    }
}