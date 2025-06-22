package com.neuromotion.citas.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorCreateRequest {
    private String usuarioId;
    @Pattern(regexp = "^[0-9]{4,6}$", message = "El CMP debe tener entre 4 y 6 dígitos")
    private String cmp;
    private String especialidadId;
    private List<String> sedeIds;
    private String fotoUrl;
}