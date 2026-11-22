package com.evaluacion.josuelopez.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.evaluacion.josuelopez.dto.ErrorResponse;
import com.evaluacion.josuelopez.dto.PaginaResponse;
import com.evaluacion.josuelopez.dto.TransferenciaRequest;
import com.evaluacion.josuelopez.dto.TransferenciaResponse;
import com.evaluacion.josuelopez.servicio.TransferenciaService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.smallrye.common.annotation.Blocking;

@Path("/api/transferencias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
@Tag(name = "Transferencias", description = "Ejecución de transferencias e historial")
public class TransferenciaResource {

    @Inject
    TransferenciaService transferenciaService;

    @POST
    @Operation(summary = "Ejecutar una transferencia",
            description = "Debita la cuenta origen y acredita la destino en una sola transacción. "
                    + "Exige cuentas existentes y activas, saldo suficiente, monto mayor a cero y límite diario de $10,000. "
                    + "Si el monto supera $5,000 o la cuenta ya hizo más de 5 transferencias en la última hora, "
                    + "la operación se registra como EN_REVISION y el dinero sí se mueve.")
    @APIResponse(responseCode = "201", description = "Transferencia registrada. El estado puede ser COMPLETADA o EN_REVISION",
            content = @Content(schema = @Schema(implementation = TransferenciaResponse.class)))
    @APIResponse(responseCode = "400", description = "Monto inválido o cuerpo mal formado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Alguna de las cuentas no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "422", description = "Regla de negocio no cumplida: cuenta inactiva, misma cuenta, saldo insuficiente o límite diario. El mensaje indica el monto restante cuando aplica",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response ejecutar(@Valid TransferenciaRequest solicitud) {
        TransferenciaResponse creada = transferenciaService.ejecutar(solicitud);
        return Response.status(Response.Status.CREATED).entity(creada).build();
    }

    @GET
    @Operation(summary = "Consultar el historial de transferencias",
            description = "Filtros opcionales por cuenta origen, cuenta destino, estado y rango de fechas calendario (America/Bogota). "
                    + "El resultado siempre llega ordenado por fecha descendente.")
    @APIResponse(responseCode = "200", description = "Página de transferencias ordenada por fecha descendente")
    @APIResponse(responseCode = "400", description = "Filtro o paginación inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PaginaResponse<TransferenciaResponse> listar(
            @Parameter(description = "Página, empezando en 0", example = "0")
            @QueryParam("pagina") @DefaultValue("0") int pagina,
            @Parameter(description = "Tamaño de página, entre 1 y 100", example = "10")
            @QueryParam("tamano") @DefaultValue("10") int tamano,
            @Parameter(description = "Filtra por el id de la cuenta origen")
            @QueryParam("cuentaOrigenId") Long cuentaOrigenId,
            @Parameter(description = "Filtra por el id de la cuenta destino")
            @QueryParam("cuentaDestinoId") Long cuentaDestinoId,
            @Parameter(description = "COMPLETADA, RECHAZADA o EN_REVISION")
            @QueryParam("estado") String estado,
            @Parameter(description = "Fecha inicial inclusive, formato yyyy-MM-dd", example = "2026-11-01")
            @QueryParam("fechaDesde") String fechaDesde,
            @Parameter(description = "Fecha final inclusive, formato yyyy-MM-dd", example = "2026-11-22")
            @QueryParam("fechaHasta") String fechaHasta) {
        Paginacion.validar(pagina, tamano);
        return transferenciaService.listar(pagina, tamano, cuentaOrigenId, cuentaDestinoId, estado, fechaDesde, fechaHasta);
    }
}
