package com.neuromotion.citas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neuromotion.citas.dto.TurnoResponse;
import com.neuromotion.citas.model.Especialidad;
import com.neuromotion.citas.model.Turno;
import com.neuromotion.citas.service.TurnoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@Slf4j
public class TurnoController {

    private final TurnoService turnoService;
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosTurnos(@RequestHeader("Authorization") String authorizationHeader) {
    try {
            List<TurnoResponse> turnos = turnoService.obtenerTodosLosTurnos(authorizationHeader);
            if (turnos.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204 No Content si no hay turnos
            }
            return ResponseEntity.ok(turnos); // 200 OK con los turnos
        } catch (Exception e) {
            // Loguear el error para depuración
            System.err.println("Error al obtener turnos: " + e.getMessage());
            return ResponseEntity.status(500).body("Error interno al obtener turnos: " + e.getMessage());
        }}

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Turno>> obtenerTurnosPorDoctor(@PathVariable String doctorId) {
        List<Turno> turnos = turnoService.obtenerTurnosPorDoctor(doctorId);
        return ResponseEntity.ok(turnos);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> crearTurno(@RequestBody Turno turno) {
         try {
            Turno nuevo = turnoService.crearTurno(turno);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Void> eliminarTurno(@PathVariable String id) {
        turnoService.eliminarTurno(id);
        return ResponseEntity.noContent().build();
    }
}