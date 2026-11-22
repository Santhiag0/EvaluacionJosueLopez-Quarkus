package com.evaluacion.josuelopez.servicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.evaluacion.josuelopez.dominio.ReglasNegocio;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DeteccionSospechosaService {

    private static final Logger log = LoggerFactory.getLogger(DeteccionSospechosaService.class);

    private final TransferenciaRepository transferenciaRepository;
    private final Clock reloj;

    @Inject
    public DeteccionSospechosaService(TransferenciaRepository transferenciaRepository, Clock reloj) {
        this.transferenciaRepository = transferenciaRepository;
        this.reloj = reloj;
    }

    // Cuenta las exitosas de la ultima hora. Con 5 previas, esta es la sexta y se marca.
    public ResultadoDeteccion evaluar(Long cuentaOrigenId, BigDecimal monto) {
        List<String> motivos = new ArrayList<>();
        if (monto.compareTo(ReglasNegocio.UMBRAL_MONTO_SOSPECHOSO) > 0) {
            motivos.add(ReglasNegocio.MOTIVO_MONTO);
        }

        LocalDateTime hasta = LocalDateTime.now(reloj);
        LocalDateTime desde = hasta.minusHours(1);
        long recientes = transferenciaRepository.contarExitosasEnVentana(cuentaOrigenId, desde, hasta);
        if (recientes >= ReglasNegocio.MAX_TRANSFERENCIAS_POR_HORA) {
            motivos.add(ReglasNegocio.MOTIVO_FRECUENCIA);
        }

        if (motivos.isEmpty()) {
            return ResultadoDeteccion.limpia();
        }
        String motivo = String.join(". ", motivos);
        log.warn("Transferencia sospechosa cuentaOrigenId={} monto={} motivo={}", cuentaOrigenId, monto, motivo);
        return new ResultadoDeteccion(true, motivo);
    }
}
