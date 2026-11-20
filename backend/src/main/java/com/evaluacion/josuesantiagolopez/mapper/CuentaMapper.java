package com.evaluacion.josuesantiagolopez.mapper;

import com.evaluacion.josuesantiagolopez.domain.Cuenta;
import com.evaluacion.josuesantiagolopez.dto.CuentaResponse;

public final class CuentaMapper {

    private CuentaMapper() {
    }

    public static CuentaResponse toResponse(Cuenta cuenta) {
        CuentaResponse respuesta = new CuentaResponse();
        respuesta.id = cuenta.id;
        respuesta.numeroCuenta = cuenta.numeroCuenta;
        respuesta.titular = cuenta.titular;
        respuesta.tipo = cuenta.tipo;
        respuesta.saldo = cuenta.saldo;
        respuesta.estado = cuenta.estado;
        respuesta.fechaCreacion = cuenta.fechaCreacion;
        return respuesta;
    }
}
