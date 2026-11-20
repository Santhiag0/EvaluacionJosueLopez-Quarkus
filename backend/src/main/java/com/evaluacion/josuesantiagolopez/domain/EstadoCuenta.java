package com.evaluacion.josuesantiagolopez.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EstadoCuenta {
    ACTIVA,
    INACTIVA;

    @JsonCreator
    public static EstadoCuenta desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El estado de la cuenta es obligatorio");
        }
        try {
            return EstadoCuenta.valueOf(TipoCuenta.normalizar(valor));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("El estado de la cuenta debe ser ACTIVA o INACTIVA");
        }
    }
}
