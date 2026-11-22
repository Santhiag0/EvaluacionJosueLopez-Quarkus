package com.evaluacion.josuelopez.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "cuentas")
public class Cuenta extends PanacheEntity {

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 10)
    public String numeroCuenta;

    @Column(name = "titular", nullable = false, length = 150)
    public String titular;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    public TipoCuenta tipo;

    @Column(name = "saldo", nullable = false, precision = 19, scale = 2)
    public BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    public EstadoCuenta estado;

    @Column(name = "fecha_creacion", nullable = false)
    public LocalDateTime fechaCreacion;
}
