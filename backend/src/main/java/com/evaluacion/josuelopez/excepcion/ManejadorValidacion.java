package com.evaluacion.josuelopez.excepcion;

import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;

// Quarkus responde esta excepcion con otro JSON. Esta prioridad deja el campo mensaje.
@Provider
@Priority(Priorities.USER - 1)
public class ManejadorValidacion implements ExceptionMapper<ResteasyReactiveViolationException> {

    @Override
    public Response toResponse(ResteasyReactiveViolationException excepcion) {
        String mensaje = excepcion.getConstraintViolations().stream()
                .map(violacion -> violacion.getMessage())
                .filter(texto -> texto != null && !texto.isBlank())
                .distinct()
                .collect(Collectors.joining(". "));
        if (mensaje.isBlank()) {
            mensaje = "La solicitud tiene datos inválidos";
        }
        return ManejadorExcepciones.respuesta(400, mensaje);
    }
}
