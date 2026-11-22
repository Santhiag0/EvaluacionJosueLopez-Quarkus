package com.evaluacion.josuelopez.repositorio;

import java.time.LocalDateTime;

import com.evaluacion.josuelopez.dominio.EstadoTransferencia;

public record FiltroTransferencia(
        Long cuentaOrigenId,
        Long cuentaDestinoId,
        EstadoTransferencia estado,
        LocalDateTime fechaDesdeInclusive,
        LocalDateTime fechaHastaExclusiva) {
}
