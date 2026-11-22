package com.evaluacion.josuelopez.dto;

import java.math.BigDecimal;

import com.evaluacion.josuelopez.dominio.EstadoCuenta;
import com.evaluacion.josuelopez.dominio.TipoCuenta;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar una cuenta bancaria")
public record CuentaRequest(
        @Schema(description = "Número único de exactamente 10 dígitos", example = "1000000001", required = true)
        @NotBlank(message = "El número de cuenta es obligatorio")
        @Pattern(regexp = "\\d{10}", message = "El número de cuenta debe tener exactamente 10 dígitos")
        String numeroCuenta,

        @Schema(description = "Nombre del titular", example = "Ana María Restrepo", required = true)
        @NotBlank(message = "El titular es obligatorio")
        @Size(max = 150, message = "El titular admite como máximo 150 caracteres")
        String titular,

        @Schema(description = "Tipo de cuenta", example = "AHORRO", required = true)
        @NotNull(message = "El tipo de cuenta es obligatorio")
        TipoCuenta tipo,

        @Schema(description = "Saldo inicial. Cero está permitido; un valor negativo no", example = "25000.00", required = true)
        @NotNull(message = "El saldo inicial es obligatorio")
        @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 17, fraction = 2, message = "El saldo admite como máximo dos decimales")
        BigDecimal saldoInicial,

        @Schema(description = "Estado inicial de la cuenta", example = "ACTIVA", required = true)
        @NotNull(message = "El estado de la cuenta es obligatorio")
        EstadoCuenta estado) {
}
