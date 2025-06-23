package com.neuromotion.administracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class UsuarioUpdateRequest {
    private String nombres;
    private String apellidos;
    @Size(min = 9, max = 9)
    private String celular;
    @Email
    private String correo;
    private String direccion;
}
