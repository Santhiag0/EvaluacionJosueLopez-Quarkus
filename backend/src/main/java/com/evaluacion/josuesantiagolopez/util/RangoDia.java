package com.evaluacion.josuesantiagolopez.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record RangoDia(Instant inicio, Instant finExclusivo) {

    public static RangoDia desde(Instant ahora, ZoneId zona) {
        LocalDate fecha = ahora.atZone(zona).toLocalDate();
        return new RangoDia(
                fecha.atStartOfDay(zona).toInstant(),
                fecha.plusDays(1).atStartOfDay(zona).toInstant());
    }
}
