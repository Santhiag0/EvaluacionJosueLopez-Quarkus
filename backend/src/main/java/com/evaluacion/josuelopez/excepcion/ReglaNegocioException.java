package com.evaluacion.josuelopez.excepcion;

public class ReglaNegocioException extends RuntimeException {

    private final int status;
    private final Long cuentaOrigenId;
    private final Long cuentaDestinoId;
    private final boolean registrarRechazo;

    public ReglaNegocioException(int status, String mensaje) {
        this(status, mensaje, null, null, false);
    }

    public ReglaNegocioException(int status, String mensaje, Long cuentaOrigenId, Long cuentaDestinoId,
            boolean registrarRechazo) {
        super(mensaje);
        this.status = status;
        this.cuentaOrigenId = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.registrarRechazo = registrarRechazo;
    }

    public int getStatus() {
        return status;
    }

    public Long getCuentaOrigenId() {
        return cuentaOrigenId;
    }

    public Long getCuentaDestinoId() {
        return cuentaDestinoId;
    }

    public boolean isRegistrarRechazo() {
        return registrarRechazo;
    }
}
