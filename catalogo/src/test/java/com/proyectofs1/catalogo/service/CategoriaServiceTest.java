package com.proyectofs1.catalogo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyectofs1.catalogo.model.Categoria;
import com.proyectofs1.catalogo.dto.CategoriaDTO;
import com.proyectofs1.catalogo.repository.CategoriaRepository;


@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void findAll_DeberiaRetornarListaDeCategorias() {
        // GIVEN
        when(categoriaRepository.findAll()).thenReturn(Collections.singletonList(new Categoria()));
        
        // WHEN: Cambiamos a List<CategoriaDTO>
        List<CategoriaDTO> resultado = categoriaService.findAll();
        
        // THEN
        assertNotNull(resultado);
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void save_DeberiaGuardarCategoria() {
        // GIVEN: Creamos un DTO, no una entidad
        CategoriaDTO catDto = new CategoriaDTO();
        catDto.setNombre("Accion");
        
        // Simulamos el guardado (asumimos que el service guarda y retorna el objeto)
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(new Categoria());
        
        // WHEN
        CategoriaDTO resultado = categoriaService.save(catDto);
        
        // THEN
        assertNotNull(resultado);
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }
    @Test
    void findById_DeberiaRetornarCategoriaDTO_CuandoIdExiste() {
        // GIVEN
        Long id = 1L;
        Categoria cat = new Categoria();
        cat.setNombre("Accion");
        // Simulamos que el repositorio encuentra el dato
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));
        
        // WHEN
        CategoriaDTO resultado = categoriaService.findById(id);
        
        // THEN
        assertNotNull(resultado);
        assertEquals("Accion", resultado.getNombre());
        verify(categoriaRepository, times(1)).findById(id);
    }

    @Test
    void findById_DeberiaRetornarNull_CuandoIdNoExiste() {
        // GIVEN
        Long id = 99L;
        // Simulamos que el repositorio NO encuentra el dato (cubre la rama del else/if)
        when(categoriaRepository.findById(id)).thenReturn(Optional.empty());
        
        // WHEN
        CategoriaDTO resultado = categoriaService.findById(id);
        
        // THEN
        assertNull(resultado);
        verify(categoriaRepository, times(1)).findById(id);
    }

    @Test
    void deleteById_DeberiaEjecutarEliminacion() {
        // GIVEN
        Long id = 1L;
        doNothing().when(categoriaRepository).deleteById(id);
        
        // WHEN
        categoriaService.deleteById(id);
        
        // THEN
        verify(categoriaRepository, times(1)).deleteById(id);
    }
    
    
}