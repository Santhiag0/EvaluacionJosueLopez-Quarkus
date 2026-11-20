package com.evaluacion.josuesantiagolopez.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
public class ValidacionExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String mensaje = exception.getConstraintViolations().stream()
                .map(ValidacionExceptionMapper::describir)
                .collect(Collectors.joining("; "));
        if (mensaje.isBlank()) {
            mensaje = "Los datos enviados no son válidos";
        }
        return RespuestasError.json(Response.Status.BAD_REQUEST.getStatusCode(), "DATOS_INVALIDOS", mensaje);
    }

    private static String describir(ConstraintViolation<?> violation) {
        String campo = violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString();
        int punto = campo.lastIndexOf('.');
        if (punto >= 0) {
            campo = campo.substring(punto + 1);
        }
        if (campo.isBlank()) {
            return violation.getMessage();
        }
        return campo + ": " + violation.getMessage();
    }
}
