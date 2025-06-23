package com.neuromotion.administracion.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.neuromotion.administracion.dto.EspecialidadRequest;
import com.neuromotion.administracion.model.Especialidad;
import com.neuromotion.administracion.service.EspecialidadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {
    private static final Logger logger = LoggerFactory.getLogger(EspecialidadController.class);

    private final EspecialidadService especialidadService;

    
    @GetMapping
    public ResponseEntity<List<Especialidad>> listar() {
       // Verificar roles
        return ResponseEntity.ok(especialidadService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Especialidad> buscar(@PathVariable String id) {
        return especialidadService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Especialidad> crear(@RequestBody EspecialidadRequest especialidad) {
        return ResponseEntity.ok(especialidadService.crear(especialidad));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        especialidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Especialidad> actualizar(@PathVariable String id, @Valid @RequestBody EspecialidadRequest especialidad) {
        logger.info("Intentando actualizar especialidad con ID: {}", id);

        try {
            Especialidad updatedEspecialidad = especialidadService.actualizar(id,especialidad);
            return ResponseEntity.ok(updatedEspecialidad);
        
        } catch (Exception e) {
            logger.warn("Especialidad con ID {} no encontrada", id);
            return ResponseEntity.notFound().build();
        }
                  
              
    }
}