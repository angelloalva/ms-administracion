package com.neuromotion.administracion.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class SedeRequest {
       @NotBlank
       @Size(min = 4)
    private String nombre;

    @NotBlank
    @Size(min = 4)
    private String direccion;
}
