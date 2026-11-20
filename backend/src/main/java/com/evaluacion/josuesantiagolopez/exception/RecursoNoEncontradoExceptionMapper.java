package com.evaluacion.josuesantiagolopez.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RecursoNoEncontradoExceptionMapper implements ExceptionMapper<RecursoNoEncontradoException> {

    @Override
    public Response toResponse(RecursoNoEncontradoException exception) {
        return RespuestasError.json(Response.Status.NOT_FOUND.getStatusCode(), "NO_ENCONTRADO", exception.getMessage());
    }
}
