package com.neuromotion.administracion.dto;

import lombok.Data;

@Data
public class PacienteResponse {
    private String id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String celular;
    private String correo;
    private String direccion;

    public PacienteResponse(String id, String tipoDocumento, String numeroDocumento, String nombres, 
                            String apellidos, String celular, String correo, String direccion) {
        this.id = id;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;                         
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.celular = celular;
        this.correo = correo;
        this.direccion = direccion;
    }

    // Getters and Setters can be added here if needed
    
}
