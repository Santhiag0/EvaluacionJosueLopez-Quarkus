package com.evaluacion.josuesantiagolopez.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.text.Normalizer;
import java.util.Locale;

public enum TipoCuenta {
    AHORRO,
    CORRIENTE;

    @JsonCreator
    public static TipoCuenta desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El tipo de cuenta es obligatorio");
        }
        String normalizado = normalizar(valor);
        try {
            return TipoCuenta.valueOf(normalizado);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("El tipo de cuenta debe ser AHORRO o CORRIENTE");
        }
    }

    static String normalizar(String valor) {
        String sinAcentos = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
