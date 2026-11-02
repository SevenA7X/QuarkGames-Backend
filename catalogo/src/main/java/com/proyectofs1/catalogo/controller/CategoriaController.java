package com.proyectofs1.catalogo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyectofs1.catalogo.dto.CategoriaDTO; // Importación del DTO
import com.proyectofs1.catalogo.service.CategoriaService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/categorias")
@CrossOrigin(origins = "*") // Previene el error CORS al consultar desde la interfaz de Swagger
@Tag(name = "2. Gestión de Categorías", description = "Endpoints para administrar las categorías y géneros a los que pertenecen los videojuegos.")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> findAll() {
        List<CategoriaDTO> categorias = categoriaService.findAll();
        if (categorias.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        // Integración HATEOAS: Enlace hacia el detalle de cada categoría en la lista
        for (CategoriaDTO categoria : categorias) {
            categoria.add(linkTo(methodOn(CategoriaController.class).findById(categoria.getId())).withSelfRel());
        }
        
        return ResponseEntity.ok(categorias);
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> save(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO nuevaCategoria = categoriaService.save(categoriaDTO);
        
        // Integración HATEOAS: Enlace a sí misma y hacia la colección completa
        nuevaCategoria.add(linkTo(methodOn(CategoriaController.class).findById(nuevaCategoria.getId())).withSelfRel());
        nuevaCategoria.add(linkTo(methodOn(CategoriaController.class).findAll()).withRel("todas-las-categorias"));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> findById(@PathVariable Long id) {
        CategoriaDTO categoriaDTO = categoriaService.findById(id);
        
        if (categoriaDTO != null) {
            // Integración HATEOAS: Enlace a sí misma y hacia la colección completa
            categoriaDTO.add(linkTo(methodOn(CategoriaController.class).findById(id)).withSelfRel());
            categoriaDTO.add(linkTo(methodOn(CategoriaController.class).findAll()).withRel("todas-las-categorias"));
            
            return ResponseEntity.ok(categoriaDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Método para ELIMINAR una categoría (No requiere HATEOAS porque retorna 204 No Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        categoriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Método para ACTUALIZAR una categoría
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> update(@PathVariable Long id, @Valid @RequestBody CategoriaDTO categoriaDTO) {
        // 1. Primero verificamos que la categoría exista
        CategoriaDTO categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 2. Le asignamos el ID de la ruta para asegurar que no se cree una nueva
        categoriaDTO.setId(id);
        
        // 3. Guardamos los cambios
        CategoriaDTO categoriaActualizada = categoriaService.save(categoriaDTO);
        
        // Integración HATEOAS: Enlace a sí misma y hacia la colección completa
        categoriaActualizada.add(linkTo(methodOn(CategoriaController.class).findById(categoriaActualizada.getId())).withSelfRel());
        categoriaActualizada.add(linkTo(methodOn(CategoriaController.class).findAll()).withRel("todas-las-categorias"));
        
        return ResponseEntity.ok(categoriaActualizada);
    }
}