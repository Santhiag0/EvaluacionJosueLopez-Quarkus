package com.evaluacion.josuelopez.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "transferencias", indexes = {
        @Index(name = "idx_transferencia_origen_fecha", columnList = "cuenta_origen_id, fecha"),
        @Index(name = "idx_transferencia_destino_fecha", columnList = "cuenta_destino_id, fecha"),
        @Index(name = "idx_transferencia_fecha", columnList = "fecha")
})
public class Transferencia extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_origen_id", nullable = false)
    public Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_destino_id", nullable = false)
    public Cuenta cuentaDestino;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    public BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    public LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    public EstadoTransferencia estado;

    @Column(name = "sospechosa", nullable = false)
    public boolean sospechosa;

    @Column(name = "motivo", length = 500)
    public String motivo;
}
