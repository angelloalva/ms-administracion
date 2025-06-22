package com.neuromotion.citas.dto;

import java.util.HashSet;
import java.util.Set;

import com.neuromotion.citas.enums.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private String id;
    private String tipoDocumento; // DNI, CE, PASAPORTE, etc.
    private String numeroDocumento; // 12345678, X12345, etc.
    private String nombres;
    private String apellidos;
    private String direccion;
    private String celular;
    private String correo;

    private String password;
    private Set<Rol> roles = new HashSet<>();
}
