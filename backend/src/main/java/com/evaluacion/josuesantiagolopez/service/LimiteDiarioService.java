package com.evaluacion.josuesantiagolopez.service;

import com.evaluacion.josuesantiagolopez.exception.BusinessException;
import com.evaluacion.josuesantiagolopez.repository.TransferenciaRepository;
import com.evaluacion.josuesantiagolopez.util.Montos;
import com.evaluacion.josuesantiagolopez.util.RangoDia;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class LimiteDiarioService {

    public static final BigDecimal LIMITE = new BigDecimal("10000.00");

    private final TransferenciaRepository transferencias;
    private final ZoneId zona;

    public LimiteDiarioService(
            TransferenciaRepository transferencias,
            @ConfigProperty(name = "app.zona-horaria", defaultValue = "America/Guayaquil") String zona) {
        this.transferencias = transferencias;
        this.zona = ZoneId.of(zona);
    }

    public void validar(Long cuentaOrigenId, BigDecimal monto, Instant ahora) {
        RangoDia rango = RangoDia.desde(ahora, zona);
        BigDecimal acumulado = transferencias.sumarExitosas(cuentaOrigenId, rango.inicio(), rango.finExclusivo());
        if (acumulado.add(monto).compareTo(LIMITE) > 0) {
            BigDecimal restante = LIMITE.subtract(acumulado);
            if (restante.signum() < 0) {
                restante = BigDecimal.ZERO.setScale(2);
            }
            throw new BusinessException(
                    422,
                    "LIMITE_DIARIO_EXCEDIDO",
                    "La transferencia supera el límite diario de $" + Montos.texto(LIMITE)
                            + ". Monto restante disponible: $" + Montos.texto(restante));
        }
    }
}
