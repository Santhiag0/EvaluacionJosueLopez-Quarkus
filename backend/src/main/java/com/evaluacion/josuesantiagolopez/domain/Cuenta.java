package com.evaluacion.josuesantiagolopez.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cuentas")
public class Cuenta extends PanacheEntity {

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 10)
    public String numeroCuenta;

    @Column(nullable = false, length = 150)
    public String titular;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public TipoCuenta tipo;

    @Column(nullable = false, precision = 16, scale = 2)
    public BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoCuenta estado;

    @Column(name = "fecha_creacion", nullable = false)
    public Instant fechaCreacion;
}
