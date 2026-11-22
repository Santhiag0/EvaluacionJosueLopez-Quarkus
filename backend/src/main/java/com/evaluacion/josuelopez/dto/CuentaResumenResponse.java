package com.evaluacion.josuelopez.dto;

import java.math.BigDecimal;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Resumen operativo de una cuenta")
public record CuentaResumenResponse(
        @Schema(example = "1") Long cuentaId,
        @Schema(example = "1000000001") String numeroCuenta,
        @Schema(example = "Ana María Restrepo") String titular,
        @Schema(description = "Saldo actual", example = "24750.00") BigDecimal saldoActual,
        @Schema(description = "Suma de transferencias exitosas emitidas hoy (completadas y en revisión)", example = "250.00")
        BigDecimal totalTransferidoHoy,
        @Schema(description = "Cantidad de transferencias exitosas emitidas hoy", example = "1")
        long cantidadTransferenciasHoy,
        @Schema(description = "Cantidad histórica de transferencias salientes marcadas como sospechosas", example = "0")
        long cantidadMarcadasSospechosas) {
}
