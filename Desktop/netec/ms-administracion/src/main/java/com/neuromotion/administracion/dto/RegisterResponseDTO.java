package com.neuromotion.administracion.dto;

import lombok.Data;

@Data
public class RegisterResponseDTO {
    private String message;
    private UsuarioResponse user;

    public RegisterResponseDTO(String message, UsuarioResponse user) {
        this.message = message;
        this.user = user;
    }
}