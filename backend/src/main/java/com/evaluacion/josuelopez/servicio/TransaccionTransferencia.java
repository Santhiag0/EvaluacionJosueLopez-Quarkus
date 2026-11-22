package com.evaluacion.josuelopez.servicio;

import java.time.Clock;
import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.evaluacion.josuelopez.dominio.Cuenta;
import com.evaluacion.josuelopez.dominio.EstadoCuenta;
import com.evaluacion.josuelopez.dominio.EstadoTransferencia;
import com.evaluacion.josuelopez.dominio.ReglasNegocio;
import com.evaluacion.josuelopez.dominio.Transferencia;
import com.evaluacion.josuelopez.dto.TransferenciaRequest;
import com.evaluacion.josuelopez.dto.TransferenciaResponse;
import com.evaluacion.josuelopez.excepcion.ReglaNegocioException;
import com.evaluacion.josuelopez.mapeador.CuentaMapper;
import com.evaluacion.josuelopez.mapeador.TransferenciaMapper;
import com.evaluacion.josuelopez.repositorio.CuentaRepository;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

@ApplicationScoped
public class TransaccionTransferencia {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final DeteccionSospechosaService deteccionSospechosaService;
    private final LimiteDiarioService limiteDiarioService;
    private final Clock reloj;

    @Inject
    public TransaccionTransferencia(CuentaRepository cuentaRepository,
            TransferenciaRepository transferenciaRepository,
            DeteccionSospechosaService deteccionSospechosaService,
            LimiteDiarioService limiteDiarioService,
            Clock reloj) {
        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.deteccionSospechosaService = deteccionSospechosaService;
        this.limiteDiarioService = limiteDiarioService;
        this.reloj = reloj;
    }

    @Transactional
    public TransferenciaResponse ejecutar(TransferenciaRequest solicitud) {
        if (solicitud.cuentaOrigenId().equals(solicitud.cuentaDestinoId())) {
            throw new ReglaNegocioException(422, "La cuenta origen y la cuenta destino deben ser diferentes");
        }

        // Siempre se bloquea primero el id menor para que dos transferencias cruzadas no se deadlockeen.
        Long primero = Math.min(solicitud.cuentaOrigenId(), solicitud.cuentaDestinoId());
        Long segundo = Math.max(solicitud.cuentaOrigenId(), solicitud.cuentaDestinoId());
        Cuenta bloqueadaPrimero = cuentaRepository.bloquearPorId(primero);
        Cuenta bloqueadaSegundo = cuentaRepository.bloquearPorId(segundo);
        Cuenta origen = solicitud.cuentaOrigenId().equals(primero) ? bloqueadaPrimero : bloqueadaSegundo;
        Cuenta destino = solicitud.cuentaDestinoId().equals(primero) ? bloqueadaPrimero : bloqueadaSegundo;

        if (origen == null) {
            throw new ReglaNegocioException(404, "No existe la cuenta origen con id " + solicitud.cuentaOrigenId());
        }
        if (destino == null) {
            throw new ReglaNegocioException(404, "No existe la cuenta destino con id " + solicitud.cuentaDestinoId());
        }

        validarActiva(origen, "origen", solicitud);
        validarActiva(destino, "destino", solicitud);

        if (origen.saldo.compareTo(solicitud.monto()) < 0) {
            throw new ReglaNegocioException(422,
                    "Saldo insuficiente en la cuenta origen. Saldo disponible: $"
                            + ReglasNegocio.dinero(origen.saldo)
                            + ", monto solicitado: $"
                            + ReglasNegocio.dinero(solicitud.monto()),
                    origen.id, destino.id, true);
        }

        limiteDiarioService.validar(origen.id, destino.id, solicitud.monto());
        ResultadoDeteccion deteccion = deteccionSospechosaService.evaluar(origen.id, solicitud.monto());

        origen.saldo = CuentaMapper.dinero(origen.saldo.subtract(solicitud.monto()));
        destino.saldo = CuentaMapper.dinero(destino.saldo.add(solicitud.monto()));

        Transferencia transferencia = new Transferencia();
        transferencia.cuentaOrigen = origen;
        transferencia.cuentaDestino = destino;
        transferencia.monto = CuentaMapper.dinero(solicitud.monto());
        transferencia.fecha = LocalDateTime.now(reloj);
        transferencia.sospechosa = deteccion.sospechosa();
        transferencia.estado = deteccion.sospechosa()
                ? EstadoTransferencia.EN_REVISION
                : EstadoTransferencia.COMPLETADA;
        transferencia.motivo = deteccion.motivo();
        transferenciaRepository.persist(transferencia);
        return TransferenciaMapper.aRespuesta(transferencia);
    }

    private static void validarActiva(Cuenta cuenta, String rol, TransferenciaRequest solicitud) {
        if (cuenta.estado != EstadoCuenta.ACTIVA) {
            throw new ReglaNegocioException(422,
                    "La cuenta " + rol + " " + cuenta.numeroCuenta + " está inactiva",
                    solicitud.cuentaOrigenId(),
                    solicitud.cuentaDestinoId(),
                    true);
        }
    }
}
