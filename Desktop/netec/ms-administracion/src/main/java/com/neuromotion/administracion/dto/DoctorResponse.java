package com.neuromotion.administracion.dto;

import com.neuromotion.administracion.model.Doctor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private String id;
    private String usuarioId;
    private String cmp;
    private String especialidadId;
    private List<String> sedeIds;
    private String fotoUrl;

    public static DoctorResponse fromDoctor(Doctor doctor) {
        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setUsuarioId(doctor.getUsuarioId());
        response.setCmp(doctor.getCmp());
        response.setEspecialidadId(doctor.getEspecialidadId());
        response.setSedeIds(doctor.getSedeIds());
        response.setFotoUrl(doctor.getFotoUrl());
        return response;
    }
}