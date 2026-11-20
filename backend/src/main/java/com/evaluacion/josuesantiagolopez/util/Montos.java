package com.evaluacion.josuesantiagolopez.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Montos {

    private Montos() {
    }

    public static BigDecimal aEscala(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    public static String texto(BigDecimal monto) {
        return aEscala(monto).toPlainString();
    }
}
