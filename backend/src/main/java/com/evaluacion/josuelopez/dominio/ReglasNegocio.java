package com.evaluacion.josuelopez.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ReglasNegocio {

    public static final BigDecimal UMBRAL_MONTO_SOSPECHOSO = new BigDecimal("5000.00");
    public static final int MAX_TRANSFERENCIAS_POR_HORA = 5;
    public static final BigDecimal LIMITE_DIARIO = new BigDecimal("10000.00");

    public static final String MOTIVO_MONTO = "El monto supera $5,000.00";
    public static final String MOTIVO_FRECUENCIA =
            "La cuenta origen acumula más de 5 transferencias en la última hora";

    private ReglasNegocio() {
    }

    public static String mensajeLimiteDiario(BigDecimal restante) {
        return "La transferencia excede el límite diario de $10,000.00. Monto restante disponible: $"
                + dinero(restante);
    }

    public static String dinero(BigDecimal valor) {
        BigDecimal normalizado = valor == null ? BigDecimal.ZERO : valor;
        return normalizado.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
