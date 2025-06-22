package com.neuromotion.administracion.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import com.neuromotion.administracion.security.DocumentoValidator;

@Documented
@Constraint(validatedBy = DocumentoValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDocumento {
    String message() default "Número de documento inválido según el tipo";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}