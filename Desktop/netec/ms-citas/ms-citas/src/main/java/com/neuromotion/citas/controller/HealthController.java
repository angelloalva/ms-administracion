package com.neuromotion.citas.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.neuromotion.citas.service.TurnoService;

import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {
    
    
    private final TurnoService turnoService; // Para verificar conexión a BD
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Verificar base de datos
            boolean dbOk = verificarBaseDatos();
            
            // Verificar conexión al microservicio de auth (opcional)
            boolean authServiceOk = verificarServicioAuth();
            
            status.put("status", "UP");
            status.put("timestamp", Instant.now());
            status.put("service", "adminitracion-service");
            status.put("version", "1.0.0");
            status.put("checks", Map.of(
                "database", dbOk ? "UP" : "DOWN",
                "auth-service", authServiceOk ? "UP" : "DOWN"
            ));
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            status.put("timestamp", Instant.now());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }
    
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        // Para Kubernetes readiness probe
        Map<String, String> status = new HashMap<>();
        status.put("status", "READY");
        status.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        // Para Kubernetes liveness probe
        Map<String, String> status = new HashMap<>();
        status.put("status", "ALIVE");
        status.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(status);
    }
    
    private boolean verificarBaseDatos() {
        try {
            // Hacer una consulta simple para verificar conexión
            turnoService.countTurnos(); // Método que retorna count de citas
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean verificarServicioAuth() {
        try {
            // Opcional: ping al servicio de auth
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:8081/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}