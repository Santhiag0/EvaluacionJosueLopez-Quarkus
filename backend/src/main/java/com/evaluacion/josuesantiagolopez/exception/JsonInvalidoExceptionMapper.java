package com.evaluacion.josuesantiagolopez.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonInvalidoExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    @Override
    public Response toResponse(JsonProcessingException exception) {
        return RespuestasError.json(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "DATOS_INVALIDOS",
                "El cuerpo de la petición no es válido. Revise tipos de cuenta, estados y formatos numéricos.");
    }
}
