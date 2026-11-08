package com.proyectofs1.catalogo.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.proyectofs1.catalogo.controller.CategoriaController;
import com.proyectofs1.catalogo.controller.InternalStockController;
import com.proyectofs1.catalogo.controller.JuegoController;
import com.proyectofs1.catalogo.dto.JuegoDTO;
import com.proyectofs1.catalogo.security.JwtAuthFilter;
import com.proyectofs1.catalogo.security.JwtValidator;
import com.proyectofs1.catalogo.service.CategoriaService;
import com.proyectofs1.catalogo.service.JuegoService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// Verifica las reglas de acceso del catálogo con tokens del MISMO formato que genera el MS Auth
// (sub = usuario, claim "rol", firmados con jwt.secret).
@WebMvcTest({JuegoController.class, CategoriaController.class, InternalStockController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtValidator.class})
class SecurityConfigTest {

    private static final String SECRETO = "WZ6zGZwI3mF/BibbuzY/pvQx2EQ+qjXOlOkJVSlvupeHfOj+kOlmGvZRkDcgsrgp";
    private static final String OTRO_SECRETO = "c2VjcmV0by1kaXN0aW50by1wYXJhLXByb2Jhci1sYS1maXJtYS1kZWwtdG9rZW4=";

    private static final String JUEGO_VALIDO = """
            {"titulo":"Hades","plataforma":"PC","precio":15000,"descripcion":"Roguelike mitológico.",
             "categoriaId":1,"stock":10,"imagen":"https://img/hades.jpg"}""";

    @Autowired private MockMvc mockMvc;
    @MockBean private JuegoService juegoService;
    @MockBean private CategoriaService categoriaService;

    // Igual que JwtService.generarToken() del MS Auth
    private String token(String secreto, String usuario, String rol, long duracionMs) {
        SecretKey clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        Date ahora = new Date();
        return Jwts.builder().subject(usuario).claim("rol", rol)
                .issuedAt(ahora).expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave).compact();
    }

    private String tokenAdmin() {
        return "Bearer " + token(SECRETO, "admin", "ADMIN", 3600000L);
    }

    private JuegoDTO juegoGuardado() {
        JuegoDTO dto = new JuegoDTO();
        dto.setId(13L);
        dto.setTitulo("Hades");
        return dto;
    }

    // ===== Lectura pública =====

    @Test
    void verJuegosNoNecesitaToken() throws Exception {
        JuegoDTO juego = juegoGuardado();
        when(juegoService.findAll()).thenReturn(List.of(juego));

        mockMvc.perform(get("/api/v1/juegos"))
                .andExpect(status().isOk());
    }

    @Test
    void verUnJuegoNoNecesitaToken() throws Exception {
        when(juegoService.findById(13L)).thenReturn(juegoGuardado());

        mockMvc.perform(get("/api/v1/juegos/13"))
                .andExpect(status().isOk());
    }

    // ===== Escritura protegida =====

    @Test
    void crearJuegoSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/v1/juegos").contentType(MediaType.APPLICATION_JSON).content(JUEGO_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Debes iniciar sesión como administrador."));

        verify(juegoService, never()).save(any());
    }

    @Test
    void crearJuegoConTokenDeAdminResponde201() throws Exception {
        when(juegoService.save(any())).thenReturn(juegoGuardado());

        mockMvc.perform(post("/api/v1/juegos").header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(JUEGO_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    void eliminarJuegoSinTokenResponde401() throws Exception {
        mockMvc.perform(delete("/api/v1/juegos/1"))
                .andExpect(status().isUnauthorized());

        verify(juegoService, never()).deleteById(any());
    }

    @Test
    void eliminarJuegoConTokenDeAdminFunciona() throws Exception {
        mockMvc.perform(delete("/api/v1/juegos/1").header("Authorization", tokenAdmin()))
                .andExpect(status().isOk());

        verify(juegoService).deleteById(1L);
    }

    @Test
    void crearCategoriaSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Terror\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFirmadoConOtraClaveResponde401() throws Exception {
        String falso = "Bearer " + token(OTRO_SECRETO, "admin", "ADMIN", 3600000L);

        mockMvc.perform(delete("/api/v1/juegos/1").header("Authorization", falso))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoResponde401() throws Exception {
        String vencido = "Bearer " + token(SECRETO, "admin", "ADMIN", -1000L);

        mockMvc.perform(delete("/api/v1/juegos/1").header("Authorization", vencido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenValidoConOtroRolResponde403() throws Exception {
        String cliente = "Bearer " + token(SECRETO, "pepe", "CLIENTE", 3600000L);

        mockMvc.perform(delete("/api/v1/juegos/1").header("Authorization", cliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permisos para esta acción."));
    }

    // ===== Endpoint interno: no usa JWT, usa X-Internal-Key =====

    @Test
    void endpointInternoFuncionaSinJwtPeroConClave() throws Exception {
        mockMvc.perform(post("/internal/juegos/descontar-stock").header("X-Internal-Key", "clave-de-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"juegoId\":1,\"cantidad\":1}]}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void endpointInternoSinClaveResponde403() throws Exception {
        mockMvc.perform(post("/internal/juegos/descontar-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"juegoId\":1,\"cantidad\":1}]}"))
                .andExpect(status().isForbidden());

        verify(juegoService, never()).descontarStock(any());
    }

    // ===== Códigos HTTP correctos (antes eran 500) =====

    @Test
    void rutaInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/v1/no-existe").header("Authorization", tokenAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void metodoIncorrectoResponde405() throws Exception {
        mockMvc.perform(post("/api/v1/juegos/1").header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(JUEGO_VALIDO))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
