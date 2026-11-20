package com.evaluacion.josuesantiagolopez.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EstadoTransferencia {
    COMPLETADA,
    RECHAZADA,
    EN_REVISION;

    @JsonCreator
    public static EstadoTransferencia desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El estado de la transferencia es obligatorio");
        }
        String normalizado = TipoCuenta.normalizar(valor);
        if ("EN_REVISION".equals(normalizado)
                || "REQUIERE_REVISION".equals(normalizado)
                || "SOSPECHOSA".equals(normalizado)) {
            return EN_REVISION;
        }
        try {
            return EstadoTransferencia.valueOf(normalizado);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "El estado debe ser COMPLETADA, RECHAZADA o EN_REVISION");
        }
    }
}
