package com.evaluacion.josuesantiagolopez.service;

public record ResultadoDeteccion(boolean sospechosa, String motivo) {

    public static ResultadoDeteccion limpia() {
        return new ResultadoDeteccion(false, null);
    }
}
