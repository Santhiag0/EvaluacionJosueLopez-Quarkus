package com.evaluacion.josuesantiagolopez.dto;

import com.evaluacion.josuesantiagolopez.domain.EstadoCuenta;
import com.evaluacion.josuesantiagolopez.domain.TipoCuenta;
import java.math.BigDecimal;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Cuenta bancaria registrada")
public class CuentaResponse {

    @Schema(example = "1")
    public Long id;

    @Schema(example = "1000000001")
    public String numeroCuenta;

    @Schema(example = "Josue Santiago Lopez")
    public String titular;

    @Schema(example = "AHORRO")
    public TipoCuenta tipo;

    @Schema(example = "1500.00")
    public BigDecimal saldo;

    @Schema(example = "ACTIVA")
    public EstadoCuenta estado;

    public Instant fechaCreacion;
}
