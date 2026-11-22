package com.evaluacion.josuelopez.mapeador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.evaluacion.josuelopez.dominio.Cuenta;
import com.evaluacion.josuelopez.dto.CuentaOperacionResponse;
import com.evaluacion.josuelopez.dto.CuentaRequest;
import com.evaluacion.josuelopez.dto.CuentaResponse;

public final class CuentaMapper {

    private CuentaMapper() {
    }

    public static Cuenta aEntidad(CuentaRequest solicitud, LocalDateTime fechaCreacion) {
        Cuenta cuenta = new Cuenta();
        cuenta.numeroCuenta = solicitud.numeroCuenta();
        cuenta.titular = solicitud.titular().trim();
        cuenta.tipo = solicitud.tipo();
        cuenta.saldo = dinero(solicitud.saldoInicial());
        cuenta.estado = solicitud.estado();
        cuenta.fechaCreacion = fechaCreacion;
        return cuenta;
    }

    public static CuentaResponse aRespuesta(Cuenta cuenta) {
        return new CuentaResponse(
                cuenta.id,
                cuenta.numeroCuenta,
                cuenta.titular,
                cuenta.tipo,
                dinero(cuenta.saldo),
                cuenta.estado,
                cuenta.fechaCreacion);
    }

    public static CuentaOperacionResponse aOperacion(Cuenta cuenta) {
        return new CuentaOperacionResponse(
                cuenta.id,
                cuenta.numeroCuenta,
                cuenta.titular,
                dinero(cuenta.saldo));
    }

    public static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
