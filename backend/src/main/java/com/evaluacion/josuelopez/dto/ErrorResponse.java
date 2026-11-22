package com.evaluacion.josuelopez.dto;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Error devuelto por la API")
public record ErrorResponse(
        @Schema(example = "2026-11-22T10:15:00-05:00") OffsetDateTime marcaTiempo,
        @Schema(example = "422") int status,
        @Schema(example = "Unprocessable Entity") String error,
        @Schema(example = "Saldo insuficiente en la cuenta origen. Saldo disponible: $100.00, monto solicitado: $250.00")
        String mensaje) {
}
