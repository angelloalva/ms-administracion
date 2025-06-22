package com.neuromotion.citas.enums;

public enum TipoDocumento {
    DNI("1"),
    PASAPORTE("2"),
    CARNET_EXTRANJERIA("3");

    private final String codigo;

    TipoDocumento(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static boolean isValid(String codigo) {
        for (TipoDocumento tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return true;
            }
        }
        return false;
    }
}
