package com.proyectofs1.catalogo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyectofs1.catalogo.model.Categoria;
import com.proyectofs1.catalogo.model.Juego;
import com.proyectofs1.catalogo.dto.JuegoDTO;
import com.proyectofs1.catalogo.repository.JuegoRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;

// La anotación habilita el uso de Mockito en esta clase de prueba
@ExtendWith(MockitoExtension.class)
class JuegoServiceTest {

    // 1. Aislamos la dependencia (La Base de Datos)
    @Mock
    private JuegoRepository juegoRepository;

    // 2. Inyectamos los mocks en el servicio que vamos a probar
    @InjectMocks

    private JuegoService juegoService;
    @Test
    void findById_DeberiaRetornarJuegoDTO_CuandoIdExiste() {
        // --- GIVEN (Dado que...) ---
        Long idBuscado = 1L;
        Juego juegoSimulado = new Juego();
        juegoSimulado.setId(idBuscado);
        juegoSimulado.setTitulo("Silent Hill 2");
        juegoSimulado.setPrecio(45000.0);
        
        // Simulamos la respuesta de la base de datos
        when(juegoRepository.findById(idBuscado)).thenReturn(Optional.of(juegoSimulado));

        // --- WHEN (Cuando...) ---
        // Ejecutamos el método real de nuestro servicio
        JuegoDTO resultado = juegoService.findById(idBuscado);

        // --- THEN (Entonces...) ---
        // Verificamos con asserts que el resultado sea el esperado
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(idBuscado, resultado.getId(), "El ID debe coincidir");
        assertEquals("Silent Hill 2", resultado.getTitulo(), "El título debe coincidir");
        
        // Verificamos que el repositorio fue llamado exactamente 1 vez
        verify(juegoRepository, times(1)).findById(idBuscado);
    }

    @Test
    void findById_DeberiaRetornarNull_CuandoIdNoExiste() {
        // --- GIVEN ---
        Long idBuscado = 99L;
        when(juegoRepository.findById(idBuscado)).thenReturn(Optional.empty());

        // --- WHEN ---
        JuegoDTO resultado = juegoService.findById(idBuscado);

        // --- THEN ---
        assertNull(resultado, "El resultado debe ser nulo si el juego no existe");
        verify(juegoRepository, times(1)).findById(idBuscado);
    }
    @Test
    void findAll_DeberiaRetornarListaDeJuegos_CuandoExistenDatos() {
        // --- GIVEN ---
        Juego juego1 = new Juego();
        juego1.setId(1L);
        juego1.setTitulo("Juego A");
        
        Juego juego2 = new Juego();
        juego2.setId(2L);
        juego2.setTitulo("Juego B");
        
        // Simulamos que la base de datos retorna una lista con 2 registros
        when(juegoRepository.findAll()).thenReturn(Arrays.asList(juego1, juego2));

        // --- WHEN ---
        List<JuegoDTO> resultado = juegoService.findAll();

        // --- THEN ---
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size(), "La lista debe contener exactamente 2 elementos");
        verify(juegoRepository, times(1)).findAll();
    }

    @Test
    void findAll_DeberiaRetornarListaVacia_CuandoNoHayDatos() {
        // --- GIVEN ---
        // Simulamos una base de datos vacía
        when(juegoRepository.findAll()).thenReturn(Collections.emptyList());

        // --- WHEN ---
        List<JuegoDTO> resultado = juegoService.findAll();

        // --- THEN ---
        assertNotNull(resultado, "La lista no debe ser nula, debe estar vacía");
        assertTrue(resultado.isEmpty(), "La lista debe estar vacía");
        verify(juegoRepository, times(1)).findAll();
    }

    @Test
    void save_DeberiaGuardarYRetornarJuegoDTOConId() {
        // --- GIVEN ---
        // 1. Datos que envía el usuario (sin ID)
        JuegoDTO dtoEntrada = new JuegoDTO();
        dtoEntrada.setTitulo("Nuevo Juego");
        dtoEntrada.setPrecio(20000.0);

        // 2. Entidad simulada que retorna la base de datos (con ID asignado)
        Juego entidadGuardada = new Juego();
        entidadGuardada.setId(5L);
        entidadGuardada.setTitulo("Nuevo Juego");
        entidadGuardada.setPrecio(20000.0);

        // Simulamos que al guardar CUALQUIER objeto de tipo Juego, retorna la entidad con ID 5
        when(juegoRepository.save(any(Juego.class))).thenReturn(entidadGuardada);

        // --- WHEN ---
        JuegoDTO resultado = juegoService.save(dtoEntrada);

        // --- THEN ---
        assertNotNull(resultado);
        assertEquals(5L, resultado.getId(), "El DTO debe contener el ID asignado por la BD");
        assertEquals("Nuevo Juego", resultado.getTitulo());
        
        // Verificamos que se invocó el método save del repositorio
        verify(juegoRepository, times(1)).save(any(Juego.class));
    }
    @Test
    void delete_DeberiaEliminarJuego_CuandoIdExiste() {
        // --- GIVEN ---
        Long idEliminar = 1L;
        // Simulamos que el repositorio ejecuta la eliminación sin retornar nada
        doNothing().when(juegoRepository).deleteById(idEliminar);

        // --- WHEN ---
        juegoService.deleteById(idEliminar);

        // --- THEN ---
        // Verificamos que el método deleteById del repositorio fue llamado exactamente 1 vez
        verify(juegoRepository, times(1)).deleteById(idEliminar);
    }
    @Test
    void buscarPorCategoria_DeberiaRetornarLista() {
        String categoria = "Accion";
        Juego juego = new Juego();
        juego.setTitulo("Test");
        when(juegoRepository.findByCategoriaNombre(categoria)).thenReturn(List.of(juego));
        
        List<JuegoDTO> resultado = juegoService.buscarPorCategoria(categoria);
        
        assertFalse(resultado.isEmpty());
        verify(juegoRepository).findByCategoriaNombre(categoria);
    }

    @Test
    void buscarPorTitulo_DeberiaRetornarLista() {
        String titulo = "Test";
        Juego juego = new Juego();
        when(juegoRepository.findByTituloContainingIgnoreCase(titulo)).thenReturn(List.of(juego));
        
        List<JuegoDTO> resultado = juegoService.buscarPorTitulo(titulo);
        
        assertFalse(resultado.isEmpty());
        verify(juegoRepository).findByTituloContainingIgnoreCase(titulo);
    }

    @Test
    void buscarPorPresupuesto_DeberiaRetornarLista() {
        double precio = 500.0;
        Juego juego = new Juego();
        when(juegoRepository.findByPrecioLessThanEqual(precio)).thenReturn(List.of(juego));
        
        List<JuegoDTO> resultado = juegoService.buscarPorPresupuesto(precio);
        
        assertFalse(resultado.isEmpty());
        verify(juegoRepository).findByPrecioLessThanEqual(precio);
    }

    
    @Test
    void save_DeberiaGuardarConCategoria_CuandoDtoTieneCategoriaId() {
        // --- GIVEN ---
        JuegoDTO dtoEntrada = new JuegoDTO();
        dtoEntrada.setTitulo("Juego con Categoria");
        dtoEntrada.setCategoriaId(1L); 
        dtoEntrada.setPrecio(20000.0); // <--- AGREGA ESTA LÍNEA

        Juego entidadGuardada = new Juego();
        entidadGuardada.setTitulo("Juego con Categoria");

        // Mock para simular el comportamiento de guardado
        when(juegoRepository.save(any(Juego.class))).thenReturn(entidadGuardada);

        // --- WHEN ---
        JuegoDTO resultado = juegoService.save(dtoEntrada);

        // --- THEN ---
        assertNotNull(resultado);
        verify(juegoRepository, times(1)).save(any(Juego.class));
    }
    @Test
void convertToDTO_DeberiaMapearCategoria_CuandoJuegoTieneCategoria() {
    // 1. GIVEN: Un juego con categoría
    Juego juego = new Juego();
    juego.setId(1L);
    juego.setTitulo("Test");
    
    Categoria cat = new Categoria();
    cat.setId(10L);
    juego.setCategoria(cat); // Esto es lo que activa la línea roja

    // 2. WHEN: Llamamos al método (como convertToDTO es privado, 
    // lo probamos a través de un método público que lo use, como findById)
    when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
    JuegoDTO dto = juegoService.findById(1L);

    // 3. THEN
    assertNotNull(dto);
    assertEquals(10L, dto.getCategoriaId());
}
}