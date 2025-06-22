package com.neuromotion.citas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.neuromotion.citas.enums.DiaSemana;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Document(collection = "turnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Turno {
    @Id
    private String id;
    
    private String especialidadId;
    private String sedeId;
    private String doctorId;
    
    private List<DiaTurno> diasDisponibles;
    
    @Data
    public static class DiaTurno {
        private LocalDate fecha;        // 2025-06-09
        private DiaSemana dia;          // LUNES (calculado automáticamente)
        private LocalTime horaInicio;   // 08:00:00
        private LocalTime horaFin;      // 12:00:00
        private List<SlotTurno> slots;  // Generados automáticamente
    }
    
    @Data
    public static class SlotTurno {
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private LocalDateTime fechaHoraCompleta;
        private boolean ocupado = false;
        private String pacienteId;
        private String citaId; // Referencia a la cita si está ocupado
    }
}