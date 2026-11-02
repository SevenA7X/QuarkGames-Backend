package com.proyectofs1.catalogo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyectofs1.catalogo.model.Juego;
import com.proyectofs1.catalogo.dto.JuegoDTO;
import com.proyectofs1.catalogo.repository.JuegoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class JuegoService {
    
    private static final Logger log = LoggerFactory.getLogger(JuegoService.class);
    
    @Autowired
    private JuegoRepository juegoRepository;

    public List<JuegoDTO> findAll() {
        log.info("Consultando todos los juegos en la base de datos.");
        return juegoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public JuegoDTO findById(Long id) {
        log.info("Iniciando búsqueda de juego con ID: {}", id);
        Optional<Juego> juego = juegoRepository.findById(id);
        if (juego.isPresent()) {
            log.info("Juego con ID: {} encontrado exitosamente.", id);
            return convertToDTO(juego.get());
        }
        log.warn("No se encontró ningún juego con el ID: {}", id);
        return null; // Permite al Controller retornar 404 Not Found
    }
    
    public JuegoDTO save(JuegoDTO juegoDTO) {
        log.info("Iniciando proceso de guardado para el juego: {}", juegoDTO.getTitulo());
        Juego juego = convertToEntity(juegoDTO);
        Juego juegoGuardado = juegoRepository.save(juego);
        log.info("Juego '{}' guardado exitosamente con ID asignado: {}", juegoGuardado.getTitulo(), juegoGuardado.getId());
        return convertToDTO(juegoGuardado);
    }
    
    public void deleteById(Long id) {
        log.info("Ejecutando eliminación del juego con ID: {}", id);
        juegoRepository.deleteById(id);
        log.info("Juego con ID: {} eliminado correctamente (si existía).", id);
    }

    public List<JuegoDTO> buscarPorCategoria(String nombreCategoria) {
        log.info("Buscando juegos por categoría: {}", nombreCategoria);
        return juegoRepository.findByCategoriaNombre(nombreCategoria).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JuegoDTO> buscarPorTitulo(String titulo) {
        log.info("Buscando juegos que contengan el título: {}", titulo);
        return juegoRepository.findByTituloContainingIgnoreCase(titulo).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JuegoDTO> buscarPorPresupuesto(double precioMaximo) {
        log.info("Buscando juegos con un presupuesto máximo de: {}", precioMaximo);
        return juegoRepository.findByPrecioLessThanEqual(precioMaximo).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /* public List<JuegoDTO> buscarPorGeneroYPresupuesto(String genero, double precioMaximo) {
        log.info("Buscando juegos filtrados por género '{}' y presupuesto máximo '{}'", genero, precioMaximo);
        return juegoRepository.buscarPorGeneroYPrecioMaximo(genero, precioMaximo).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    } */
    
    

    // --- Métodos Privados de Mapeo ---
    
    private JuegoDTO convertToDTO(Juego juego) {
        JuegoDTO dto = new JuegoDTO();
        dto.setId(juego.getId());
        dto.setTitulo(juego.getTitulo());
        dto.setPrecio(juego.getPrecio());
        dto.setDescripcion(juego.getDescripcion());
        
        // Mapeo de la relación usando el ID para el DTO
        if (juego.getCategoria() != null) {
            dto.setCategoriaId(juego.getCategoria().getId());
        }
        return dto;
    }

    private Juego convertToEntity(JuegoDTO dto) {
        Juego juego = new Juego();
        juego.setId(dto.getId());
        juego.setTitulo(dto.getTitulo());
        juego.setPrecio(dto.getPrecio());
        juego.setDescripcion(dto.getDescripcion());
        
        // Reconstrucción de la relación para JPA
        if (dto.getCategoriaId() != null) {
            // Se crea una instancia de Categoria solo con el ID para que Hibernate sepa la llave foránea
            com.proyectofs1.catalogo.model.Categoria categoria = new com.proyectofs1.catalogo.model.Categoria();
            categoria.setId(dto.getCategoriaId());
            juego.setCategoria(categoria);
        }
        return juego;
    }
}
