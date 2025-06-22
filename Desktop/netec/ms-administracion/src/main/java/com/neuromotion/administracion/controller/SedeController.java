package com.neuromotion.administracion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neuromotion.administracion.dto.MensajeResponse;
import com.neuromotion.administracion.dto.SedeRequest;
import com.neuromotion.administracion.dto.SedeResponse;
import com.neuromotion.administracion.model.Sede;
import com.neuromotion.administracion.service.SedeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
public class SedeController {
    private final SedeService sedeService;
    // Crear una nueva sede (solo admin)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> crearSede(@Valid @RequestBody SedeRequest request) {
       return sedeService.crearSede(request);
    }

    // Obtener una sede por ID (acceso público)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerSedePorId(@PathVariable String id) {
        return sedeService.obtenerSedePorId(id);
        
    }

    // Obtener todas las sedes (acceso público)
    @GetMapping
    public List<Sede> obtenerTodasLasSedes() {
        return sedeService.obtenerTodasLasSedes();
        
    }

    // Actualizar una sede parcialmente (solo admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> actualizarSede(@PathVariable String id, @RequestBody SedeRequest request) {
           return sedeService.actualizarSede(id, request);
        
    }

    // Eliminar una sede (solo admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> eliminarSede(@PathVariable String id) {
      
            sedeService.eliminarSede(id);
            return ResponseEntity.ok(new MensajeResponse("Eliminado con éxito"));
       
    }
}