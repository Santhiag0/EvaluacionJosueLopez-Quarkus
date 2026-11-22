package com.evaluacion.josuelopez.excepcion;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.RollbackException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.evaluacion.josuelopez.dto.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ManejadorExcepciones implements ExceptionMapper<Exception> {

    private static final Logger log = LoggerFactory.getLogger(ManejadorExcepciones.class);
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Override
    public Response toResponse(Exception excepcion) {
        if (excepcion instanceof ReglaNegocioException regla) {
            return respuesta(regla.getStatus(), regla.getMessage());
        }
        if (excepcion instanceof ConstraintViolationException violaciones) {
            String mensaje = violaciones.getConstraintViolations().stream()
                    .map(violacion -> violacion.getMessage())
                    .distinct()
                    .collect(Collectors.joining(". "));
            if (mensaje.isBlank()) {
                mensaje = "La solicitud tiene datos inválidos";
            }
            return respuesta(400, mensaje);
        }
        if (excepcion instanceof WebApplicationException web) {
            int status = web.getResponse().getStatus();
            if (web.getResponse().hasEntity()) {
                return web.getResponse();
            }
            String mensaje = web.getMessage() == null ? razon(status) : web.getMessage();
            return respuesta(status, mensaje);
        }
        if (esViolacionDeUnicidad(excepcion)) {
            return respuesta(409, "Ya existe un registro con esos datos únicos");
        }
        log.error("Error no controlado al procesar la solicitud", excepcion);
        return respuesta(500, "Ocurrió un error interno al procesar la solicitud");
    }

    private static boolean esViolacionDeUnicidad(Throwable error) {
        Throwable actual = error;
        while (actual != null) {
            if (actual instanceof org.hibernate.exception.ConstraintViolationException
                    || actual instanceof java.sql.SQLIntegrityConstraintViolationException) {
                return true;
            }
            if (actual instanceof PersistenceException || actual instanceof RollbackException) {
                actual = actual.getCause();
                continue;
            }
            actual = actual.getCause();
        }
        return false;
    }

    static Response respuesta(int status, String mensaje) {
        ErrorResponse cuerpo = new ErrorResponse(
                OffsetDateTime.now(ZONA),
                status,
                razon(status),
                mensaje);
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(cuerpo).build();
    }

    static String razon(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable Entity";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }
}
