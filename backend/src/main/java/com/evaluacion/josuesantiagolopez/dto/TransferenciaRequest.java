package com.evaluacion.josuesantiagolopez.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Solicitud para transferir dinero entre dos cuentas")
public class TransferenciaRequest {

    @NotNull(message = "La cuenta origen es obligatoria")
    @Schema(description = "Identificador de la cuenta que envía el dinero", example = "1")
    public Long cuentaOrigenId;

    @NotNull(message = "La cuenta destino es obligatoria")
    @Schema(description = "Identificador de la cuenta que recibe el dinero", example = "2")
    public Long cuentaDestinoId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 14, fraction = 2, message = "El monto admite como máximo dos decimales")
    @Schema(description = "Monto a transferir, mayor a cero", example = "150.50")
    public BigDecimal monto;
}
