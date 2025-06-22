package com.neuromotion.citas.model;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neuromotion.citas.enums.EstadoCita;


@Data
@Document(collection = "citas")
public class Cita {

    @Id
    private String id;

    private String pacienteId;     // Referencia al paciente (puede ser ObjectId en String)
    private String doctorId;       // Referencia al doctor
    private String sedeId;         // Referencia a la sede donde se hará la cita
@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
private LocalDateTime fechaHora;

    private String turnoId;        // Opcional, si quieres referenciar el turno asignado

    private EstadoCita estado;         // Estado de la cita (e.g. PENDIENTE, CONFIRMADA, CANCELADA)

    private String observaciones;  // Campo para notas adicionales
}