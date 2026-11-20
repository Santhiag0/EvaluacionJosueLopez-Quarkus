package com.evaluacion.josuesantiagolopez.mapper;

import com.evaluacion.josuesantiagolopez.domain.Transferencia;
import com.evaluacion.josuesantiagolopez.dto.TransferenciaResponse;

public final class TransferenciaMapper {

    private TransferenciaMapper() {
    }

    public static TransferenciaResponse toResponse(Transferencia transferencia) {
        TransferenciaResponse respuesta = new TransferenciaResponse();
        respuesta.id = transferencia.id;
        respuesta.cuentaOrigenId = transferencia.cuentaOrigen.id;
        respuesta.numeroCuentaOrigen = transferencia.cuentaOrigen.numeroCuenta;
        respuesta.titularOrigen = transferencia.cuentaOrigen.titular;
        respuesta.cuentaDestinoId = transferencia.cuentaDestino.id;
        respuesta.numeroCuentaDestino = transferencia.cuentaDestino.numeroCuenta;
        respuesta.titularDestino = transferencia.cuentaDestino.titular;
        respuesta.monto = transferencia.monto;
        respuesta.estado = transferencia.estado;
        respuesta.sospechosa = transferencia.sospechosa;
        respuesta.motivoSospecha = transferencia.motivoSospecha;
        respuesta.motivoRechazo = transferencia.motivoRechazo;
        respuesta.fecha = transferencia.fecha;
        return respuesta;
    }
}
