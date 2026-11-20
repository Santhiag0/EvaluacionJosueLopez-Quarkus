package com.evaluacion.josuesantiagolopez.service;

import com.evaluacion.josuesantiagolopez.domain.EstadoTransferencia;
import java.time.Instant;

public class FiltroTransferencia {

    public Long cuentaOrigenId;
    public Long cuentaDestinoId;
    public EstadoTransferencia estado;
    public Instant fechaDesde;
    public Instant fechaHastaExclusivo;
}
