package com.neuromotion.administracion.dto;

import lombok.Data;

@Data
public class SedeResponse{
private String id;
    private String nombre;
    private String direccion;

    public SedeResponse(String id,String nombre, String direccion) {
                                this.id=id;
        this.nombre = nombre;
      
        this.direccion = direccion;
    }
}