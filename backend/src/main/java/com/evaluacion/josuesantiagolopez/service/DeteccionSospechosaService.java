package com.evaluacion.josuesantiagolopez.service;

import com.evaluacion.josuesantiagolopez.domain.Cuenta;
import com.evaluacion.josuesantiagolopez.repository.TransferenciaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DeteccionSospechosaService {

    public static final BigDecimal UMBRAL_MONTO = new BigDecimal("5000.00");
    public static final int UMBRAL_CANTIDAD_HORA = 5;

    private final TransferenciaRepository transferencias;

    public DeteccionSospechosaService(TransferenciaRepository transferencias) {
        this.transferencias = transferencias;
    }

    public ResultadoDeteccion evaluar(Cuenta origen, BigDecimal monto, Instant ahora) {
        long previas = transferencias.contarExitosasDesde(origen.id, ahora.minus(1, ChronoUnit.HOURS));
        return evaluar(monto, previas);
    }

    public ResultadoDeteccion evaluar(BigDecimal monto, long transferenciasUltimaHora) {
        List<String> motivos = new ArrayList<>();
        if (monto.compareTo(UMBRAL_MONTO) > 0) {
            motivos.add("El monto supera $5000.00");
        }
        if (transferenciasUltimaHora > UMBRAL_CANTIDAD_HORA) {
            motivos.add("La cuenta origen acumula más de 5 transferencias en la última hora");
        }
        if (motivos.isEmpty()) {
            return ResultadoDeteccion.limpia();
        }
        return new ResultadoDeteccion(true, String.join(". ", motivos));
    }
}
