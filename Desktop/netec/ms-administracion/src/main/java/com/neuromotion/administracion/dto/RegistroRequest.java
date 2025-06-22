package com.neuromotion.administracion.dto;


import java.util.HashSet;
import java.util.Set;

import com.neuromotion.administracion.enums.Rol;
import com.neuromotion.administracion.validation.ValidDocumento;

import jakarta.validation.constraints.*;
import lombok.Data;
@Data
@ValidDocumento

public class RegistroRequest {

    @NotNull(message = "El tipo de documento es obligatorio")
    private String documentoTipo;

    @NotBlank(message = "El número de documento es obligatorio")
    private String documentoNumero;

    @NotBlank
    private String nombres;

    @NotBlank
    private String apellidos;

    @NotBlank
    @Email
    private String correo;


    @Size(min = 9, max = 9)
    private String celular;

    @Size(min = 8,max=8, message = "La contraseña debe tener  8 caracteres")
    private String password;


    private String direccion;
    private Set<Rol> roles = new HashSet<>();
    // Getters y setters
}