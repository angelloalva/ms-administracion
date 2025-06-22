package com.neuromotion.citas.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;
@Document(collection = "doctores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    private String id;

    private String usuarioId;         // Referencia al usuario
    private String cmp;
    private String especialidadId;    // Referencia a la especialidad
    private List<String> sedeIds;     // Lista de sedes donde trabaja
    private String fotoUrl;           // URL o ruta de la foto

}