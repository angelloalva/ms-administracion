package com.neuromotion.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class EspecialidadRequest {
    
    @NotBlank
       @Size(min = 4)
    private String nombre;

    @NotBlank
    @Size(min = 4)
    private String descripcion;
}
