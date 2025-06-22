package com.neuromotion.administracion.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.neuromotion.administracion.enums.Rol;


import lombok.*;

@Document(collection = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    private String id;
    private String tipoDocumento; // DNI, CE, PASAPORTE, etc.

    @Indexed(unique = true)
    private String numeroDocumento; // 12345678, X12345, etc.

    private String nombres;
    private String apellidos;
    private String direccion;
    private String celular;

    @Indexed(unique = true)
    private String correo;

    private String password;
    private Set<Rol> roles = new HashSet<>();

    public String getUsername() {
        return tipoDocumento + "-" + numeroDocumento;
    }
}