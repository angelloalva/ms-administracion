package com.neuromotion.administracion.service;

import com.neuromotion.administracion.dto.MensajeResponse;
import com.neuromotion.administracion.dto.SedeRequest;
import com.neuromotion.administracion.dto.SedeResponse;
import com.neuromotion.administracion.exceptions.DocumentoNoEncontradoException;
import com.neuromotion.administracion.model.Sede;
import com.neuromotion.administracion.repository.SedeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SedeService {

    
    private final SedeRepository sedeRepository;

    // Crear una nueva sede a partir de un DTO de creación
    public ResponseEntity<?> crearSede(SedeRequest request) {
        try {
            Sede sede = new Sede();
            sede.setNombre(request.getNombre());
            sede.setDireccion(request.getDireccion());
            sede = sedeRepository.save(sede);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(sede);
        } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeResponse("Error al crear la sede: " + e.getMessage()));
        }
     
       
    }

    // Obtener una sede por ID
    public ResponseEntity<?> obtenerSedePorId(String id) {
        Optional<Sede> sede = sedeRepository.findById(id);
        if (sede.isEmpty()) {
           throw new DocumentoNoEncontradoException("Sede no encontrado");
        }
        Sede actual = sede.get();
        return ResponseEntity.ok(actual);
    }

    // Obtener todas las sedes
    public List<Sede> obtenerTodasLasSedes() {
        return sedeRepository.findAll();
    }

    // Actualizar una sede parcialmente
   public ResponseEntity<?> actualizarSede(String id, SedeRequest request) {
    try {
        Sede sedeExistente = sedeRepository.findById(id)
            .orElseThrow(() -> new DocumentoNoEncontradoException("Sede no encontrada con ID: " + id));

        if (request.getNombre() != null) {
            sedeExistente.setNombre(request.getNombre());
        }
        if (request.getDireccion() != null) {
            sedeExistente.setDireccion(request.getDireccion());
        }
        sedeRepository.save(sedeExistente);
        return ResponseEntity.ok(sedeExistente);
    } catch (DocumentoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new MensajeResponse(e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new MensajeResponse("Error al actualizar la sede: " + e.getMessage()));
    }
}

    // Eliminar una sede por ID
    public void eliminarSede(String id) {
        if (!sedeRepository.existsById(id)) {
            throw new DocumentoNoEncontradoException("Sede no encontrada con ID: " + id);
        }
        sedeRepository.deleteById(id);
    }
    public void countSedes() {
        long count = sedeRepository.count();
        if (count == 0) {
            throw new DocumentoNoEncontradoException("No hay sedes registradas");
        }   
    }
}