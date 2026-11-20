package com.evaluacion.josuesantiagolopez.dto;

import com.evaluacion.josuesantiagolopez.domain.EstadoCuenta;
import com.evaluacion.josuesantiagolopez.domain.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Datos para registrar una cuenta bancaria")
public class CuentaRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "\\d{10}", message = "El número de cuenta debe tener exactamente 10 dígitos")
    @Schema(description = "Número único de 10 dígitos", example = "1000000001")
    public String numeroCuenta;

    @NotBlank(message = "El titular es obligatorio")
    @Size(max = 150, message = "El titular admite como máximo 150 caracteres")
    @Schema(description = "Nombre del titular", example = "Josue Santiago Lopez")
    public String titular;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    @Schema(description = "Tipo de cuenta: AHORRO o CORRIENTE", example = "AHORRO")
    public TipoCuenta tipo;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "El saldo inicial no puede ser negativo")
    @Digits(integer = 14, fraction = 2, message = "El saldo admite como máximo dos decimales")
    @Schema(description = "Saldo inicial, mayor o igual a cero", example = "1500.00")
    public BigDecimal saldoInicial;

    @NotNull(message = "El estado es obligatorio")
    @Schema(description = "Estado: ACTIVA o INACTIVA", example = "ACTIVA")
    public EstadoCuenta estado;
}
