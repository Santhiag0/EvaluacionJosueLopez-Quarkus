package com.evaluacion.josuesantiagolopez.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transferencias", indexes = {
        @Index(name = "idx_transf_origen_fecha", columnList = "cuenta_origen_id, fecha"),
        @Index(name = "idx_transf_destino_fecha", columnList = "cuenta_destino_id, fecha"),
        @Index(name = "idx_transf_estado", columnList = "estado")
})
public class Transferencia extends PanacheEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id", nullable = false)
    public Cuenta cuentaOrigen;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id", nullable = false)
    public Cuenta cuentaDestino;

    @Column(nullable = false, precision = 16, scale = 2)
    public BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoTransferencia estado;

    @Column(nullable = false)
    public boolean sospechosa;

    @Column(name = "motivo_sospecha", length = 500)
    public String motivoSospecha;

    @Column(name = "motivo_rechazo", length = 500)
    public String motivoRechazo;

    @Column(nullable = false)
    public Instant fecha;
}
