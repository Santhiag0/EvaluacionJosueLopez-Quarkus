package com.evaluacion.josuelopez.dto;

import java.math.BigDecimal;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para transferir dinero entre dos cuentas")
public record TransferenciaRequest(
        @Schema(description = "Identificador de la cuenta que envía", example = "1", required = true)
        @NotNull(message = "La cuenta origen es obligatoria")
        Long cuentaOrigenId,

        @Schema(description = "Identificador de la cuenta que recibe", example = "2", required = true)
        @NotNull(message = "La cuenta destino es obligatoria")
        Long cuentaDestinoId,

        @Schema(description = "Monto a transferir. Debe ser mayor a cero y tener como máximo dos decimales", example = "250.00", required = true)
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        @Digits(integer = 17, fraction = 2, message = "El monto admite como máximo dos decimales")
        BigDecimal monto) {
}
