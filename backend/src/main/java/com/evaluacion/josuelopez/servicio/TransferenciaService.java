package com.evaluacion.josuelopez.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.evaluacion.josuelopez.dominio.EstadoTransferencia;
import com.evaluacion.josuelopez.dto.PaginaResponse;
import com.evaluacion.josuelopez.dto.TransferenciaRequest;
import com.evaluacion.josuelopez.dto.TransferenciaResponse;
import com.evaluacion.josuelopez.excepcion.ReglaNegocioException;
import com.evaluacion.josuelopez.mapeador.TransferenciaMapper;
import com.evaluacion.josuelopez.repositorio.FiltroTransferencia;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@ApplicationScoped
public class TransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaService.class);

    private final TransaccionTransferencia transaccionTransferencia;
    private final RechazoTransferenciaService rechazoTransferenciaService;
    private final TransferenciaRepository transferenciaRepository;

    @Inject
    public TransferenciaService(TransaccionTransferencia transaccionTransferencia,
            RechazoTransferenciaService rechazoTransferenciaService,
            TransferenciaRepository transferenciaRepository) {
        this.transaccionTransferencia = transaccionTransferencia;
        this.rechazoTransferenciaService = rechazoTransferenciaService;
        this.transferenciaRepository = transferenciaRepository;
    }

    public TransferenciaResponse ejecutar(TransferenciaRequest solicitud) {
        MDC.put("operacion", "transferencia");
        MDC.put("cuentaOrigenId", String.valueOf(solicitud.cuentaOrigenId()));
        MDC.put("cuentaDestinoId", String.valueOf(solicitud.cuentaDestinoId()));
        try {
            TransferenciaResponse respuesta = transaccionTransferencia.ejecutar(solicitud);
            log.info("Transferencia registrada id={} monto={} estado={} sospechosa={}",
                    respuesta.id(), respuesta.monto(), respuesta.estado(), respuesta.sospechosa());
            return respuesta;
        } catch (ReglaNegocioException excepcion) {
            log.warn("Transferencia rechazada monto={} status={} motivo={}",
                    solicitud.monto(), excepcion.getStatus(), excepcion.getMessage());
            // El rechazo se confirma en otra transacción: la de negocio ya hizo rollback y soltó los bloqueos.
            if (excepcion.isRegistrarRechazo()) {
                rechazoTransferenciaService.registrar(
                        excepcion.getCuentaOrigenId(),
                        excepcion.getCuentaDestinoId(),
                        solicitud.monto(),
                        excepcion.getMessage());
            }
            throw excepcion;
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public PaginaResponse<TransferenciaResponse> listar(int pagina, int tamano, Long cuentaOrigenId,
            Long cuentaDestinoId, String estado, String fechaDesde, String fechaHasta) {
        LocalDate desde = parseFecha(fechaDesde, "fechaDesde");
        LocalDate hasta = parseFecha(fechaHasta, "fechaHasta");
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new ReglaNegocioException(400, "fechaHasta no puede ser anterior a fechaDesde");
        }
        FiltroTransferencia filtro = new FiltroTransferencia(
                cuentaOrigenId,
                cuentaDestinoId,
                parseEstado(estado),
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay());
        List<TransferenciaResponse> contenido = transferenciaRepository.buscar(filtro, pagina, tamano).stream()
                .map(TransferenciaMapper::aRespuesta)
                .toList();
        return PaginaResponse.de(contenido, pagina, tamano, transferenciaRepository.contar(filtro));
    }

    private static EstadoTransferencia parseEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoTransferencia.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException excepcion) {
            throw new ReglaNegocioException(400, "El estado debe ser COMPLETADA, RECHAZADA o EN_REVISION");
        }
    }

    private static LocalDate parseFecha(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException excepcion) {
            throw new ReglaNegocioException(400, nombre + " debe tener formato yyyy-MM-dd");
        }
    }
}
