package com.neuromotion.citas.enums;

import java.time.DayOfWeek;

public enum DiaSemana {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO;

  public DayOfWeek toJavaDayOfWeek() {
    switch (this) {
        case LUNES: return DayOfWeek.MONDAY;
        case MARTES: return DayOfWeek.TUESDAY;
        case MIERCOLES: return DayOfWeek.WEDNESDAY;
        case JUEVES: return DayOfWeek.THURSDAY;
        case VIERNES: return DayOfWeek.FRIDAY;
        case SABADO: return DayOfWeek.SATURDAY;
        case DOMINGO: return DayOfWeek.SUNDAY;
        default: throw new IllegalArgumentException("Día no válido: " + this);
    }
}

public static DiaSemana fromJavaDayOfWeek(DayOfWeek dayOfWeek) {
    switch (dayOfWeek) {
        case MONDAY: return LUNES;
        case TUESDAY: return MARTES;
        case WEDNESDAY: return MIERCOLES;
        case THURSDAY: return JUEVES;
        case FRIDAY: return VIERNES;
        case SATURDAY: return SABADO;
        case SUNDAY: return DOMINGO;
        default: throw new IllegalArgumentException("Día no válido: " + dayOfWeek);
    }
}
}
