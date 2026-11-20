package com.evaluacion.josuesantiagolopez.exception;

import com.evaluacion.josuesantiagolopez.dto.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;

final class RespuestasError {

    private RespuestasError() {
    }

    static Response json(int status, String codigo, String mensaje) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(codigo, mensaje, Instant.now()))
                .build();
    }
}
