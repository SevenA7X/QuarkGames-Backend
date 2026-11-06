package com.proyectofs1.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyectofs1.auth.config.SecurityConfig;
import com.proyectofs1.auth.dto.LoginResponseDTO;
import com.proyectofs1.auth.exception.CredencialesInvalidasException;
import com.proyectofs1.auth.security.JwtAuthFilter;
import com.proyectofs1.auth.security.JwtService;
import com.proyectofs1.auth.service.AuthService;

// @WebMvcTest levanta SOLO la capa web (controlador, filtros, @RestControllerAdvice).
// AuthService se reemplaza por un mock; JwtService es real para probar el filtro.
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    @MockBean private AuthService authService;

    @Test
    void loginCorrectoResponde200ConToken() throws Exception {
        when(authService.login(any()))
                .thenReturn(new LoginResponseDTO("abc.def.ghi", "Bearer", "admin", "ADMIN", 3600000L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"clave\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc.def.ghi"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario").value("admin"));
    }

    @Test
    void loginConClaveIncorrectaResponde401() throws Exception {
        when(authService.login(any())).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos."));
    }

    @Test
    void loginConCamposVaciosResponde400ConErrorPorCampo() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"\",\"clave\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.usuario").value("Ingresa tu usuario."))
                .andExpect(jsonPath("$.errores.clave").value("Ingresa tu contraseña."));
    }

    @Test
    void loginConJsonMalFormadoResponde400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void errorInesperadoResponde500SinDetallesInternos() throws Exception {
        when(authService.login(any())).thenThrow(new RuntimeException("se cayó la BD"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"clave\":\"1234\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error interno en el servidor."));
    }

    @Test
    void validarConTokenValidoResponde200() throws Exception {
        String token = jwtService.generarToken("admin", "ADMIN");

        mockMvc.perform(get("/api/v1/auth/validar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void validarSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validar"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token ausente, inválido o expirado."));
    }

    @Test
    void validarConTokenAlteradoResponde401() throws Exception {
        // Cambiamos un carácter en medio de la firma (3ª parte del JWT)
        String original = jwtService.generarToken("admin", "ADMIN");
        int pos = original.lastIndexOf('.') + 10;
        char nuevo = original.charAt(pos) == 'A' ? 'B' : 'A';
        String token = original.substring(0, pos) + nuevo + original.substring(pos + 1);

        mockMvc.perform(get("/api/v1/auth/validar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaInexistenteResponde404() throws Exception {
        String token = jwtService.generarToken("admin", "ADMIN");

        mockMvc.perform(get("/api/v1/auth/no-existe")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe."));
    }

    @Test
    void metodoIncorrectoResponde405() throws Exception {
        String token = jwtService.generarToken("admin", "ADMIN");

        // /login existe, pero solo acepta POST
        mockMvc.perform(get("/api/v1/auth/login")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void validarConRolDistintoDeAdminResponde403() throws Exception {
        String token = jwtService.generarToken("cliente", "CLIENTE");

        mockMvc.perform(get("/api/v1/auth/validar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}