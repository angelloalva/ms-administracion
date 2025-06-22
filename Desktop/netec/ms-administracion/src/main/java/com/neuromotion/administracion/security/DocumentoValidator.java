package com.neuromotion.administracion.security;

import com.neuromotion.administracion.dto.RegistroRequest;
import com.neuromotion.administracion.enums.TipoDocumento;
import com.neuromotion.administracion.validation.ValidDocumento;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DocumentoValidator implements ConstraintValidator<ValidDocumento, RegistroRequest> {

    @Override
    public boolean isValid(RegistroRequest value, ConstraintValidatorContext context) {
        if (value == null) return true; // O false si quieres que no acepte nulos

        String numero = value.getDocumentoNumero();
        String tipo = value.getDocumentoTipo();

        if (tipo == null || numero == null) {
            return false;
        }

        switch (tipo) {
            case "1":
                return numero.matches("\\d{8}"); // 8 dígitos
            case "2":
                return numero.matches("[A-Za-z]{1}\\d{8}"); // 1 letra + 8 dígitos
            case "3":
                return numero.matches("\\d{12}"); // 12 dígitos
            default:
                return false;
        }
    }
}
