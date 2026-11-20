package com.evaluacion.josuesantiagolopez.dto;

import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Error de la API")
public class ErrorResponse {

    @Schema(example = "LIMITE_DIARIO_EXCEDIDO")
    public String codigo;

    @Schema(example = "La transferencia supera el límite diario de $10000.00. Monto restante disponible: $2500.00")
    public String mensaje;

    @Schema(example = "2026-11-20T15:30:00Z")
    public Instant timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String codigo, String mensaje, Instant timestamp) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.timestamp = timestamp;
    }
}
