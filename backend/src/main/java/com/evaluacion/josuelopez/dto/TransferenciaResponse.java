package com.evaluacion.josuelopez.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.evaluacion.josuelopez.dominio.EstadoTransferencia;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Transferencia registrada. EN_REVISION indica que el dinero se movió y la operación quedó marcada para revisión")
public record TransferenciaResponse(
        @Schema(example = "10") Long id,
        CuentaOperacionResponse cuentaOrigen,
        CuentaOperacionResponse cuentaDestino,
        @Schema(example = "250.00") BigDecimal monto,
        LocalDateTime fecha,
        @Schema(example = "COMPLETADA") EstadoTransferencia estado,
        @Schema(example = "false") boolean sospechosa,
        @Schema(description = "Motivo de revisión o de rechazo. Nulo cuando la transferencia se completó sin observaciones", example = "El monto supera $5,000.00")
        String motivo) {
}
