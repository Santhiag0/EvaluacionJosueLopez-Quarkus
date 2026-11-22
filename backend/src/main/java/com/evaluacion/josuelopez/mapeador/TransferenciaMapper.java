package com.evaluacion.josuelopez.mapeador;

import com.evaluacion.josuelopez.dominio.Transferencia;
import com.evaluacion.josuelopez.dto.TransferenciaResponse;

public final class TransferenciaMapper {

    private TransferenciaMapper() {
    }

    public static TransferenciaResponse aRespuesta(Transferencia transferencia) {
        return new TransferenciaResponse(
                transferencia.id,
                CuentaMapper.aOperacion(transferencia.cuentaOrigen),
                CuentaMapper.aOperacion(transferencia.cuentaDestino),
                CuentaMapper.dinero(transferencia.monto),
                transferencia.fecha,
                transferencia.estado,
                transferencia.sospechosa,
                transferencia.motivo);
    }
}
