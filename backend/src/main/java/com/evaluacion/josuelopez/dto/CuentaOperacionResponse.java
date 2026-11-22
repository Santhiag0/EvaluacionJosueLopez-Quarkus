package com.evaluacion.josuelopez.dto;

import java.math.BigDecimal;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Cuenta participante en una transferencia. El saldo es el saldo actual, no el histórico")
public record CuentaOperacionResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1000000001") String numeroCuenta,
        @Schema(example = "Ana María Restrepo") String titular,
        @Schema(description = "Saldo actual de la cuenta", example = "24750.00") BigDecimal saldoActual) {
}
