package com.evaluacion.josuelopez.servicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.evaluacion.josuelopez.dominio.ReglasNegocio;
import com.evaluacion.josuelopez.excepcion.ReglaNegocioException;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LimiteDiarioService {

    private static final Logger log = LoggerFactory.getLogger(LimiteDiarioService.class);

    private final TransferenciaRepository transferenciaRepository;
    private final Clock reloj;

    @Inject
    public LimiteDiarioService(TransferenciaRepository transferenciaRepository, Clock reloj) {
        this.transferenciaRepository = transferenciaRepository;
        this.reloj = reloj;
    }

    public BigDecimal acumuladoHoy(Long cuentaOrigenId) {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDateTime desde = hoy.atStartOfDay();
        LocalDateTime hasta = hoy.plusDays(1).atStartOfDay();
        return transferenciaRepository.sumarMontoExitoso(cuentaOrigenId, desde, hasta);
    }

    public BigDecimal montoRestante(Long cuentaOrigenId) {
        BigDecimal restante = ReglasNegocio.LIMITE_DIARIO.subtract(acumuladoHoy(cuentaOrigenId));
        if (restante.signum() < 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return restante;
    }

    public void validar(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto) {
        BigDecimal acumulado = acumuladoHoy(cuentaOrigenId);
        BigDecimal proyectado = acumulado.add(monto);
        if (proyectado.compareTo(ReglasNegocio.LIMITE_DIARIO) > 0) {
            BigDecimal restante = ReglasNegocio.LIMITE_DIARIO.subtract(acumulado);
            if (restante.signum() < 0) {
                restante = BigDecimal.ZERO;
            }
            String mensaje = ReglasNegocio.mensajeLimiteDiario(restante);
            log.warn("Límite diario rechazado cuentaOrigenId={} acumulado={} monto={} restante={}",
                    cuentaOrigenId, acumulado, monto, ReglasNegocio.dinero(restante));
            throw new ReglaNegocioException(422, mensaje, cuentaOrigenId, cuentaDestinoId, true);
        }
    }
}
