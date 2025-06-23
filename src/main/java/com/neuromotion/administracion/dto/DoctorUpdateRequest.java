package com.neuromotion.administracion.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorUpdateRequest {
    
    // El CMP puede ser opcional, pero si se envía, debe cumplir el patrón
    @Pattern(regexp = "^[0-9]{4,6}$", message = "El CMP debe tener entre 4 y 6 dígitos")
    private String cmp;

    @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
    private String nombres;
        
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;
    private String especialidadId;
    private List<String> sedeIds;
    private String fotoUrl;
    // Note: CMP no se puede actualizar una vez creado
}
