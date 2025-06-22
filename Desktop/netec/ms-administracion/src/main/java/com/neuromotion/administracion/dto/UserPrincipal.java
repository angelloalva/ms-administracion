package com.neuromotion.administracion.dto;

import java.util.List;

import lombok.Data;
@Data
public class UserPrincipal {
    private String username;
    private String usuarioId;
    private String nombre;
    private String tipoDocumento;
    private String numeroDocumento;
    private List<String> roles;

    public UserPrincipal(String username, String usuarioId, String nombre, 
                        String tipoDocumento, String numeroDocumento, List<String> roles) {
        this.username = username;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.roles = roles;
    }
}
