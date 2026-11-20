package com.evaluacion.josuesantiagolopez.dto;

import com.evaluacion.josuesantiagolopez.domain.EstadoTransferencia;
import java.math.BigDecimal;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Transferencia registrada")
public class TransferenciaResponse {

    @Schema(example = "10")
    public Long id;

    @Schema(example = "1")
    public Long cuentaOrigenId;

    @Schema(example = "1000000001")
    public String numeroCuentaOrigen;

    @Schema(example = "Ana Pérez")
    public String titularOrigen;

    @Schema(example = "2")
    public Long cuentaDestinoId;

    @Schema(example = "1000000002")
    public String numeroCuentaDestino;

    @Schema(example = "Luis Gómez")
    public String titularDestino;

    @Schema(example = "150.50")
    public BigDecimal monto;

    @Schema(description = "COMPLETADA, RECHAZADA o EN_REVISION", example = "COMPLETADA")
    public EstadoTransferencia estado;

    @Schema(description = "Verdadero cuando la operación requiere revisión", example = "false")
    public boolean sospechosa;

    @Schema(example = "El monto supera $5000.00")
    public String motivoSospecha;

    @Schema(example = "La cuenta origen no tiene saldo suficiente. Saldo disponible: $20.00")
    public String motivoRechazo;

    public Instant fecha;
}
