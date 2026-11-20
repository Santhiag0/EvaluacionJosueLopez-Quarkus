package com.evaluacion.josuesantiagolopez.util;

import com.evaluacion.josuesantiagolopez.exception.BusinessException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class FechasConsulta {

    private FechasConsulta() {
    }

    public static Instant inicio(String valor, ZoneId zona) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            if (valor.length() == 10) {
                return LocalDate.parse(valor).atStartOfDay(zona).toInstant();
            }
            return Instant.parse(valor);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(400, "FECHA_INVALIDA",
                    "La fecha '" + valor + "' no es válida. Use yyyy-MM-dd o un instante ISO-8601");
        }
    }

    public static Instant finExclusivo(String valor, ZoneId zona) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            if (valor.length() == 10) {
                return LocalDate.parse(valor).plusDays(1).atStartOfDay(zona).toInstant();
            }
            return Instant.parse(valor);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(400, "FECHA_INVALIDA",
                    "La fecha '" + valor + "' no es válida. Use yyyy-MM-dd o un instante ISO-8601");
        }
    }
}
