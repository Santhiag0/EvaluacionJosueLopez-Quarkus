package com.evaluacion.josuesantiagolopez.service;

import com.evaluacion.josuesantiagolopez.domain.Cuenta;
import com.evaluacion.josuesantiagolopez.domain.EstadoCuenta;
import com.evaluacion.josuesantiagolopez.domain.EstadoTransferencia;
import com.evaluacion.josuesantiagolopez.domain.Transferencia;
import com.evaluacion.josuesantiagolopez.dto.PaginaResponse;
import com.evaluacion.josuesantiagolopez.dto.TransferenciaRequest;
import com.evaluacion.josuesantiagolopez.dto.TransferenciaResponse;
import com.evaluacion.josuesantiagolopez.exception.BusinessException;
import com.evaluacion.josuesantiagolopez.exception.RecursoNoEncontradoException;
import com.evaluacion.josuesantiagolopez.mapper.TransferenciaMapper;
import com.evaluacion.josuesantiagolopez.repository.CuentaRepository;
import com.evaluacion.josuesantiagolopez.repository.TransferenciaRepository;
import com.evaluacion.josuesantiagolopez.util.Montos;
import com.evaluacion.josuesantiagolopez.util.Paginacion;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class TransferenciaService {

    private static final Logger LOG = Logger.getLogger(TransferenciaService.class.getName());

    private final CuentaRepository cuentas;
    private final TransferenciaRepository transferencias;
    private final DeteccionSospechosaService deteccionSospechosaService;
    private final LimiteDiarioService limiteDiarioService;
    private final Clock reloj;

    public TransferenciaService(
            CuentaRepository cuentas,
            TransferenciaRepository transferencias,
            DeteccionSospechosaService deteccionSospechosaService,
            LimiteDiarioService limiteDiarioService,
            Clock reloj) {
        this.cuentas = cuentas;
        this.transferencias = transferencias;
        this.deteccionSospechosaService = deteccionSospechosaService;
        this.limiteDiarioService = limiteDiarioService;
        this.reloj = reloj;
    }

    /**
     * Debita y acredita en la misma transacción. Si la operación se rechaza, no cambia saldos
     * y deja registrada la transferencia con estado RECHAZADA.
     * dontRollbackOn permite confirmar ese registro cuando se lanza la excepción de negocio.
     */
    @Transactional(dontRollbackOn = BusinessException.class)
    public TransferenciaResponse ejecutar(TransferenciaRequest solicitud) {
        BigDecimal monto = normalizarMonto(solicitud.monto);
        if (solicitud.cuentaOrigenId.equals(solicitud.cuentaDestinoId)) {
            throw new BusinessException(400, "CUENTAS_IGUALES",
                    "La cuenta origen debe ser diferente de la cuenta destino");
        }

        Map<Long, Cuenta> bloqueadas = bloquearEnOrden(solicitud.cuentaOrigenId, solicitud.cuentaDestinoId);
        Cuenta origen = bloqueadas.get(solicitud.cuentaOrigenId);
        Cuenta destino = bloqueadas.get(solicitud.cuentaDestinoId);
        Instant ahora = Instant.now(reloj);

        if (origen.estado != EstadoCuenta.ACTIVA) {
            String mensaje = "La cuenta origen " + origen.numeroCuenta + " está inactiva";
            rechazar(origen, destino, monto, mensaje, ahora);
            throw new BusinessException(422, "CUENTA_ORIGEN_INACTIVA", mensaje);
        }
        if (destino.estado != EstadoCuenta.ACTIVA) {
            String mensaje = "La cuenta destino " + destino.numeroCuenta + " está inactiva";
            rechazar(origen, destino, monto, mensaje, ahora);
            throw new BusinessException(422, "CUENTA_DESTINO_INACTIVA", mensaje);
        }

        try {
            limiteDiarioService.validar(origen.id, monto, ahora);
        } catch (BusinessException ex) {
            rechazar(origen, destino, monto, ex.getMessage(), ahora);
            throw ex;
        }

        if (origen.saldo.compareTo(monto) < 0) {
            String mensaje = "La cuenta origen no tiene saldo suficiente. Saldo disponible: $"
                    + Montos.texto(origen.saldo);
            rechazar(origen, destino, monto, mensaje, ahora);
            throw new BusinessException(422, "SALDO_INSUFICIENTE", mensaje);
        }

        ResultadoDeteccion resultado = deteccionSospechosaService.evaluar(origen, monto, ahora);
        origen.saldo = Montos.aEscala(origen.saldo.subtract(monto));
        destino.saldo = Montos.aEscala(destino.saldo.add(monto));

        Transferencia transferencia = new Transferencia();
        transferencia.cuentaOrigen = origen;
        transferencia.cuentaDestino = destino;
        transferencia.monto = monto;
        transferencia.fecha = ahora;
        transferencia.sospechosa = resultado.sospechosa();
        transferencia.motivoSospecha = resultado.motivo();
        transferencia.estado = resultado.sospechosa()
                ? EstadoTransferencia.EN_REVISION
                : EstadoTransferencia.COMPLETADA;
        transferencias.persist(transferencia);

        if (resultado.sospechosa()) {
            LOG.warning(String.format(
                    "deteccion_sospechosa origen=%s numero=%s destino=%s monto=%s motivos=\"%s\"",
                    origen.id, origen.numeroCuenta, destino.numeroCuenta, Montos.texto(monto), resultado.motivo()));
        }
        LOG.info(String.format(
                "transferencia_ejecutada id=%s origen=%s destino=%s monto=%s estado=%s sospechosa=%s",
                transferencia.id, origen.numeroCuenta, destino.numeroCuenta,
                Montos.texto(monto), transferencia.estado, transferencia.sospechosa));
        return TransferenciaMapper.toResponse(transferencia);
    }

    @Transactional
    public PaginaResponse<TransferenciaResponse> listar(FiltroTransferencia filtro, int page, int size) {
        Paginacion.validar(page, size);
        PanacheQuery<Transferencia> consulta = transferencias.buscar(filtro).page(Page.of(page, size));
        List<TransferenciaResponse> contenido = consulta.list().stream()
                .map(TransferenciaMapper::toResponse)
                .toList();
        return PaginaResponse.of(contenido, page, size, consulta.count());
    }

    private Map<Long, Cuenta> bloquearEnOrden(Long idOrigen, Long idDestino) {
        List<Long> ids = idOrigen < idDestino ? List.of(idOrigen, idDestino) : List.of(idDestino, idOrigen);
        Map<Long, Cuenta> cuentasBloqueadas = new HashMap<>();
        for (Long id : ids) {
            Cuenta cuenta = cuentas.findByIdForUpdate(id).orElseThrow(() -> {
                String rol = id.equals(idOrigen) ? "origen" : "destino";
                return new RecursoNoEncontradoException("No existe la cuenta " + rol + " con id " + id);
            });
            cuentasBloqueadas.put(id, cuenta);
        }
        return cuentasBloqueadas;
    }

    private void rechazar(Cuenta origen, Cuenta destino, BigDecimal monto, String motivo, Instant ahora) {
        Transferencia transferencia = new Transferencia();
        transferencia.cuentaOrigen = origen;
        transferencia.cuentaDestino = destino;
        transferencia.monto = monto;
        transferencia.fecha = ahora;
        transferencia.estado = EstadoTransferencia.RECHAZADA;
        transferencia.sospechosa = false;
        transferencia.motivoRechazo = motivo;
        transferencias.persist(transferencia);
        LOG.info(String.format(
                "transferencia_rechazada origen=%s destino=%s monto=%s motivo=\"%s\"",
                origen.numeroCuenta, destino.numeroCuenta, Montos.texto(monto), motivo));
    }

    private static BigDecimal normalizarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "MONTO_INVALIDO", "El monto debe ser mayor a cero");
        }
        return Montos.aEscala(monto);
    }
}
