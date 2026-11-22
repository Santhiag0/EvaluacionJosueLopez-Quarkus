package com.evaluacion.josuelopez.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.evaluacion.josuelopez.dto.CuentaRequest;
import com.evaluacion.josuelopez.dto.CuentaResponse;
import com.evaluacion.josuelopez.dto.CuentaResumenResponse;
import com.evaluacion.josuelopez.dto.ErrorResponse;
import com.evaluacion.josuelopez.dto.PaginaResponse;
import com.evaluacion.josuelopez.servicio.CuentaService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.smallrye.common.annotation.Blocking;

@Path("/api/cuentas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
@Tag(name = "Cuentas", description = "Registro, consulta y resumen de cuentas bancarias")
public class CuentaResource {

    @Inject
    CuentaService cuentaService;

    @POST
    @Operation(summary = "Registrar una cuenta bancaria",
            description = "Crea una cuenta con número único de 10 dígitos, titular, tipo, saldo inicial y estado. Rechaza duplicados y saldos negativos.")
    @APIResponse(responseCode = "201", description = "Cuenta creada",
            content = @Content(schema = @Schema(implementation = CuentaResponse.class)))
    @APIResponse(responseCode = "400", description = "Datos inválidos, por ejemplo saldo negativo o número que no tiene 10 dígitos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "409", description = "Ya existe una cuenta con ese número",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response crear(@Valid CuentaRequest solicitud) {
        CuentaResponse creada = cuentaService.crear(solicitud);
        return Response.status(Response.Status.CREATED).entity(creada).build();
    }

    @GET
    @Operation(summary = "Listar cuentas",
            description = "Devuelve las cuentas paginadas. El ordenamiento por saldo acepta dirección ascendente o descendente.")
    @APIResponse(responseCode = "200", description = "Página de cuentas")
    @APIResponse(responseCode = "400", description = "Paginación u ordenamiento inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PaginaResponse<CuentaResponse> listar(
            @Parameter(description = "Página, empezando en 0", example = "0")
            @QueryParam("pagina") @DefaultValue("0") int pagina,
            @Parameter(description = "Tamaño de página, entre 1 y 100", example = "10")
            @QueryParam("tamano") @DefaultValue("10") int tamano,
            @Parameter(description = "Campo de orden: numeroCuenta, titular, tipo, saldo, estado o fechaCreacion", example = "saldo")
            @QueryParam("ordenarPor") @DefaultValue("numeroCuenta") String ordenarPor,
            @Parameter(description = "Dirección: asc o desc", example = "desc")
            @QueryParam("direccion") @DefaultValue("asc") String direccion) {
        Paginacion.validar(pagina, tamano);
        return cuentaService.listar(pagina, tamano, ordenarPor, direccion);
    }

    @GET
    @Path("/{id}/resumen")
    @Operation(summary = "Resumen operativo de una cuenta",
            description = "Calcula en el momento el saldo, lo transferido hoy, la cantidad de transferencias del día y las marcadas como sospechosas.")
    @APIResponse(responseCode = "200", description = "Resumen calculado",
            content = @Content(schema = @Schema(implementation = CuentaResumenResponse.class)))
    @APIResponse(responseCode = "404", description = "La cuenta no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CuentaResumenResponse resumen(
            @Parameter(description = "Identificador de la cuenta", example = "1")
            @PathParam("id") Long id) {
        return cuentaService.resumen(id);
    }
}
