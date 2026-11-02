package com.proyectofs1.catalogo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyectofs1.catalogo.model.Categoria;
import com.proyectofs1.catalogo.dto.CategoriaDTO;
import com.proyectofs1.catalogo.repository.CategoriaRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<CategoriaDTO> findAll() {
        log.info("Consultando todas las categorías en la base de datos.");
        return categoriaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO findById(Long id) {
        log.info("Iniciando búsqueda de categoría con ID: {}", id);
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        if (categoria.isPresent()) {
            log.info("Categoría con ID: {} encontrada exitosamente.", id);
            return convertToDTO(categoria.get());
        }
        log.warn("No se encontró ninguna categoría con el ID: {}", id);
        return null;
    }

    public CategoriaDTO save(CategoriaDTO categoriaDTO) {
        log.info("Iniciando proceso de guardado para la categoría: {}", categoriaDTO.getNombre());
        Categoria categoria = convertToEntity(categoriaDTO);
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        log.info("Categoría '{}' guardada exitosamente con ID asignado: {}", categoriaGuardada.getNombre(), categoriaGuardada.getId());
        return convertToDTO(categoriaGuardada);
    }

    public void deleteById(Long id) {
        log.info("Ejecutando eliminación de la categoría con ID: {}", id);
        categoriaRepository.deleteById(id);
        log.info("Categoría con ID: {} eliminada correctamente.", id);
    }

    // --- Métodos de Conversión ---

    private CategoriaDTO convertToDTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        return dto;
    }

    private Categoria convertToEntity(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setId(dto.getId());
        categoria.setNombre(dto.getNombre());
        return categoria;
    }
}