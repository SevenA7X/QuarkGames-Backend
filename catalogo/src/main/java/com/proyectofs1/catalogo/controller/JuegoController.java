package com.proyectofs1.catalogo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyectofs1.catalogo.dto.JuegoDTO; // Importación del DTO
import com.proyectofs1.catalogo.service.JuegoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/juegos")
@Tag(name = "1. Gestión de Juegos", description = "Endpoints para crear, buscar, actualizar y eliminar videojuegos del catálogo.")
public class JuegoController {
    
    @Autowired
    private JuegoService juegoService;

    @GetMapping
    public ResponseEntity<List<JuegoDTO>> findAll() {
        List<JuegoDTO> juegos = juegoService.findAll();
        if(juegos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        // Integración HATEOAS: Enlace hacia el detalle de cada juego en la lista
        for (JuegoDTO juego : juegos) {
            juego.add(linkTo(methodOn(JuegoController.class).findById(juego.getId())).withSelfRel());
        }
        
        return ResponseEntity.ok(juegos);
    }

    @PostMapping
    public ResponseEntity<JuegoDTO> save(@Valid @RequestBody JuegoDTO juegoDTO) {
        JuegoDTO juegoNuevo = juegoService.save(juegoDTO);
        
        // Integración HATEOAS: Enlaces hacia el juego creado y hacia la lista completa
        juegoNuevo.add(linkTo(methodOn(JuegoController.class).findById(juegoNuevo.getId())).withSelfRel());
        juegoNuevo.add(linkTo(methodOn(JuegoController.class).findAll()).withRel("todos-los-juegos"));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(juegoNuevo);
    }

    // --- AQUÍ ESTÁ EL PASO 1 ACOPLADO ---
    // Método crucial para que la validación de Feign (Microservicio Reseña) funcione
    @GetMapping("/{id}")
    public ResponseEntity<JuegoDTO> findById(@PathVariable Long id) {
        JuegoDTO juegoDTO = juegoService.findById(id);
        
      
        if (juegoDTO != null) {
            // Integración HATEOAS: Enlace a sí mismo y hacia la colección completa
            juegoDTO.add(linkTo(methodOn(JuegoController.class).findById(id)).withSelfRel());
            juegoDTO.add(linkTo(methodOn(JuegoController.class).findAll()).withRel("todos-los-juegos"));
            
            return ResponseEntity.ok(juegoDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<JuegoDTO> update(@PathVariable Long id, @Valid @RequestBody JuegoDTO juegoDTO) {
        juegoDTO.setId(id); 
        JuegoDTO juegoActualizado = juegoService.save(juegoDTO);
        
        if (juegoActualizado != null) {
            // Integración HATEOAS: Enlace a sí mismo y hacia la colección completa
            juegoActualizado.add(linkTo(methodOn(JuegoController.class).findById(juegoActualizado.getId())).withSelfRel());
            juegoActualizado.add(linkTo(methodOn(JuegoController.class).findAll()).withRel("todos-los-juegos"));
            
            return ResponseEntity.ok(juegoActualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> borrar(@PathVariable Long id) {
        juegoService.deleteById(id);
        return ResponseEntity.ok("Juego Eliminado");
    }
}