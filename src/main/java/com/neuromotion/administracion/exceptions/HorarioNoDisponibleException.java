package com.neuromotion.administracion.exceptions;

import java.time.LocalDateTime;
import java.util.List;

public class HorarioNoDisponibleException extends RuntimeException {
    private final List<LocalDateTime> horariosDisponibles;

    public HorarioNoDisponibleException(String message, List<LocalDateTime> horariosDisponibles) {
        super(message);
        this.horariosDisponibles = horariosDisponibles;
    }

    public List<LocalDateTime> getHorariosDisponibles() {
        return horariosDisponibles;
    }
}