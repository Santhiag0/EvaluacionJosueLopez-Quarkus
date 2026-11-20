package com.evaluacion.josuesantiagolopez.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GeneralExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException web) {
            return web.getResponse();
        }
        if (exception.getClass().getSimpleName().toLowerCase().contains("violation")) {
            return RespuestasError.json(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    "DATOS_INVALIDOS",
                    mensajeValidacion(exception));
        }
        LOG.log(Level.SEVERE, "error_no_controlado", exception);
        return RespuestasError.json(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "ERROR_INTERNO",
                "Error interno al procesar la solicitud");
    }

    private static String mensajeValidacion(Exception exception) {
        String mensaje = exception.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            return "Los datos enviados no son válidos";
        }
        return mensaje;
    }
}
