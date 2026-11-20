package com.evaluacion.josuesantiagolopez.exception;

public class BusinessException extends RuntimeException {

    private final int status;
    private final String codigo;

    public BusinessException(int status, String codigo, String mensaje) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }

    public int status() {
        return status;
    }

    public String codigo() {
        return codigo;
    }
}
