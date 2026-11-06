package com.proyectofs1.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyectofs1.auth.model.Administrador;
import com.proyectofs1.auth.repository.AdministradorRepository;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock private AdministradorRepository repository;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    void creaElAdminConLaClaveEncriptadaSiNoExiste() {
        when(repository.existsByUsuario("admin")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("$2a$10$hash");

        new DataLoader(repository, passwordEncoder, "admin", "1234").run();

        // ArgumentCaptor "atrapa" el objeto que se pasó a save() para revisarlo
        ArgumentCaptor<Administrador> captor = ArgumentCaptor.forClass(Administrador.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isEqualTo("admin");
        assertThat(captor.getValue().getClave()).isEqualTo("$2a$10$hash"); // nunca "1234"
        assertThat(captor.getValue().getRol()).isEqualTo("ADMIN");
    }

    @Test
    void noDuplicaElAdminSiYaExiste() {
        when(repository.existsByUsuario("admin")).thenReturn(true);

        new DataLoader(repository, passwordEncoder, "admin", "1234").run();

        verify(repository, never()).save(any());
    }
}