package com.evaluacion.josuesantiagolopez.dto;

import com.evaluacion.josuesantiagolopez.domain.EstadoCuenta;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Cambio de estado de una cuenta")
public class ActualizarEstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    @Schema(description = "Nuevo estado: ACTIVA o INACTIVA", example = "INACTIVA")
    public EstadoCuenta estado;
}
