package com.evaluacion.josuesantiagolopez.dto;

import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Resumen operativo de una cuenta en el día calendario America/Guayaquil")
public class ResumenCuentaResponse {

    @Schema(example = "1")
    public Long cuentaId;

    @Schema(example = "1000000001")
    public String numeroCuenta;

    @Schema(example = "Ana Pérez")
    public String titular;

    @Schema(description = "Saldo actual de la cuenta", example = "18500.00")
    public BigDecimal saldoActual;

    @Schema(description = "Suma de transferencias exitosas originadas hoy", example = "1500.00")
    public BigDecimal totalTransferidoHoy;

    @Schema(description = "Cantidad de transferencias exitosas originadas hoy", example = "2")
    public long cantidadTransferenciasDelDia;

    @Schema(description = "Cantidad de transferencias de hoy marcadas como sospechosas", example = "1")
    public long cantidadMarcadasComoSospechosas;
}
