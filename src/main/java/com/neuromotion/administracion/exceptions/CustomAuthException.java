package com.neuromotion.administracion.exceptions;

public class CustomAuthException extends RuntimeException {
    public final int status;

    public CustomAuthException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}