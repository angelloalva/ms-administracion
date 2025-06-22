package com.neuromotion.citas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.neuromotion.citas.model.Especialidad;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadValidationResponse {
    private boolean exists;
    private String message;
    private Especialidad especialidadEncontrada;
    private List<Especialidad> especialidadesSimilares;
}