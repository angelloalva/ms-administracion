package com.neuromotion.citas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.neuromotion.citas.enums.DiaSemana;
import com.neuromotion.citas.model.Doctor;
import com.neuromotion.citas.model.Especialidad;
import com.neuromotion.citas.model.Sede;
import com.neuromotion.citas.model.Turno;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponse {
    private String id;
    private String especialidadId;
    private String sedeId;
    private String doctorId;
    private String doctorNombres;
    private String doctorApellidos;
    private String doctorCmp;
    private String sedeNombre;
    private String especialidadNombre; // Opcional, si quieres incluir el nombre de la especialidad
    private List<DiaTurnoResponse> diasDisponibles;
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class DiaTurnoResponse {
        private LocalDate fecha;
        private DiaSemana dia;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private List<SlotTurnoResponse> slots;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class SlotTurnoResponse {
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private LocalDateTime fechaHoraCompleta;
        private boolean ocupado;
        private String pacienteId;
        private String citaId;
    }

    public static TurnoResponse fromTurno(Turno turno, UsuarioResponse doctor, Sede sede,Especialidad especialidad,DoctorResponse doctorInfo) {
        return new TurnoResponse(
            turno.getId(),
            turno.getEspecialidadId(),
            turno.getSedeId(),
            turno.getDoctorId(),
            doctor != null ? doctor.getNombres() : "Desconocido",
            doctor != null ? doctor.getApellidos() : "",
            doctorInfo != null ? doctorInfo.getCmp() : "Sin CMP",
            sede != null ? sede.getNombre() : "Sin sede",
            especialidad != null ? especialidad.getNombre() : "Sin especialidad",
            turno.getDiasDisponibles().stream()
                .map(dia -> new DiaTurnoResponse(
                    dia.getFecha(),
                    dia.getDia(),
                    dia.getHoraInicio(),
                    dia.getHoraFin(),
                    dia.getSlots().stream()
                        .map(slot -> new SlotTurnoResponse(
                            slot.getHoraInicio(),
                            slot.getHoraFin(),
                            slot.getFechaHoraCompleta(),
                            slot.isOcupado(),
                            slot.getPacienteId(),
                            slot.getCitaId()
                        )).collect(Collectors.toList())
                )).collect(Collectors.toList())
        );
    }
}