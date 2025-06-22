package com.neuromotion.citas.exceptions;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(String message) {
        super(message);
    }
}