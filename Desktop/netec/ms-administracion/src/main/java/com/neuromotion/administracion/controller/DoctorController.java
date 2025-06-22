package com.neuromotion.administracion.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.neuromotion.administracion.dto.DoctorCreateRequest;
import com.neuromotion.administracion.dto.DoctorResponse;
import com.neuromotion.administracion.dto.DoctorUpdateRequest;
import com.neuromotion.administracion.enums.Rol;
import com.neuromotion.administracion.model.Doctor;
import com.neuromotion.administracion.model.Usuario;
import com.neuromotion.administracion.repository.DoctorRepository;
import com.neuromotion.administracion.service.DoctorService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctores")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PACIENTE')")
public class DoctorController {
    
  
    private final DoctorService doctorService;
    
    
    // Crear doctor (solo ADMIN)
    /*@PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> crearDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        try {
            DoctorResponse doctor = doctorService.crearDoctor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(doctor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }*/
    
    // Obtener todos los doctores
    @GetMapping
    public ResponseEntity<List<DoctorResponse>> obtenerTodosLosDoctores() {
        List<DoctorResponse> doctores = doctorService.obtenerTodosLosDoctores();
        return ResponseEntity.ok(doctores);
    }
    
    // Obtener doctor por ID
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> obtenerDoctorPorId(@PathVariable String id) {
        return doctorService.obtenerDoctorPorId(id)
                .map(doctor -> ResponseEntity.ok(doctor))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Obtener doctor por CMP
    @GetMapping("/cmp/{cmp}")
    public ResponseEntity<DoctorResponse> obtenerDoctorPorCmp(@PathVariable String cmp) {
        return doctorService.obtenerDoctorPorCmp(cmp)
                .map(doctor -> ResponseEntity.ok(doctor))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Obtener doctores por especialidad
    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<DoctorResponse>> obtenerDoctoresPorEspecialidad(@PathVariable String especialidad) {
        List<DoctorResponse> doctores = doctorService.obtenerDoctoresPorEspecialidad(especialidad);
        return ResponseEntity.ok(doctores);
    }
    
    // Obtener doctores por sede
    @GetMapping("/sede/{sedeId}")
    public ResponseEntity<List<DoctorResponse>> obtenerDoctoresPorSede(@PathVariable String sedeId) {
        List<DoctorResponse> doctores = doctorService.obtenerDoctoresPorSede(sedeId);
        return ResponseEntity.ok(doctores);
    }
    
    // Buscar doctores por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<DoctorResponse>> buscarDoctoresPorNombre(@RequestParam String nombre) {
        List<DoctorResponse> doctores = doctorService.buscarDoctoresPorNombre(nombre);
        return ResponseEntity.ok(doctores);
    }
    
    // Actualizar doctor
    /*@PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @doctorService.obtenerDoctorPorId(#id).isPresent() and @doctorService.obtenerDoctorPorId(#id).get().cmp == authentication.principal.username.split('\\|')[1])")
    public ResponseEntity<DoctorResponse> actualizarDoctor(@PathVariable String id, 
                                                          @Valid @RequestBody DoctorUpdateRequest request) {
        return doctorService.actualizarDoctor(id, request)
                .map(doctor -> ResponseEntity.ok(doctor))
                .orElse(ResponseEntity.notFound().build());
    }
    */
    // Eliminar doctor (solo ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarDoctor(@PathVariable String id) {
        if (doctorService.eliminarDoctor(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    
    @GetMapping("/mis-pacientes")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Usuario>> obtenerMiUsuarioId(Authentication authentication,@RequestHeader("Authorization") String authorizationHeader) {
       /*  Object detailsObj = authentication.getDetails();
        String usuarioId = null;
        if (detailsObj instanceof Map<?, ?> details) {
            usuarioId = (String) details.get("usuarioId");
        }*/
        String usuarioId = (String) authentication.getPrincipal();
        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo obtener el usuarioId");
        }
        return ResponseEntity.ok(doctorService.obtenerPacientesDeDoctor(usuarioId,authorizationHeader));
    }
}