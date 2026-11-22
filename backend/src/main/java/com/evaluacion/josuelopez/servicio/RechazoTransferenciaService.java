package com.evaluacion.josuelopez.servicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.evaluacion.josuelopez.dominio.Cuenta;
import com.evaluacion.josuelopez.dominio.EstadoTransferencia;
import com.evaluacion.josuelopez.dominio.Transferencia;
import com.evaluacion.josuelopez.mapeador.CuentaMapper;
import com.evaluacion.josuelopez.repositorio.CuentaRepository;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RechazoTransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(RechazoTransferenciaService.class);

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final Clock reloj;

    @Inject
    public RechazoTransferenciaService(CuentaRepository cuentaRepository,
            TransferenciaRepository transferenciaRepository,
            Clock reloj) {
        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.reloj = reloj;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registrar(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto, String motivo) {
        Cuenta origen = cuentaRepository.findById(cuentaOrigenId);
        Cuenta destino = cuentaRepository.findById(cuentaDestinoId);
        if (origen == null || destino == null) {
            log.warn("No se guardó el rechazo porque una de las cuentas ya no existe origen={} destino={}",
                    cuentaOrigenId, cuentaDestinoId);
            return;
        }
        Transferencia transferencia = new Transferencia();
        transferencia.cuentaOrigen = origen;
        transferencia.cuentaDestino = destino;
        transferencia.monto = CuentaMapper.dinero(monto);
        transferencia.fecha = LocalDateTime.now(reloj);
        transferencia.estado = EstadoTransferencia.RECHAZADA;
        transferencia.sospechosa = false;
        transferencia.motivo = motivo;
        transferenciaRepository.persist(transferencia);
        log.info("Rechazo registrado id={} origen={} destino={} monto={}",
                transferencia.id, cuentaOrigenId, cuentaDestinoId, transferencia.monto);
    }
}
