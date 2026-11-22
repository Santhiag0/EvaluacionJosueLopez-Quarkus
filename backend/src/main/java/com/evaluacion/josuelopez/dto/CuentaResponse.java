package com.evaluacion.josuelopez.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.evaluacion.josuelopez.dominio.EstadoCuenta;
import com.evaluacion.josuelopez.dominio.TipoCuenta;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Cuenta bancaria registrada")
public record CuentaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1000000001") String numeroCuenta,
        @Schema(example = "Ana María Restrepo") String titular,
        @Schema(example = "AHORRO") TipoCuenta tipo,
        @Schema(example = "24750.00") BigDecimal saldo,
        @Schema(example = "ACTIVA") EstadoCuenta estado,
        LocalDateTime fechaCreacion) {
}
