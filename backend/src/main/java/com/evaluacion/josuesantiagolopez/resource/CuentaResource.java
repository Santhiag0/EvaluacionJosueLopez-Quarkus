package com.evaluacion.josuesantiagolopez.resource;

import com.evaluacion.josuesantiagolopez.dto.ActualizarEstadoRequest;
import com.evaluacion.josuesantiagolopez.dto.CuentaRequest;
import com.evaluacion.josuesantiagolopez.dto.CuentaResponse;
import com.evaluacion.josuesantiagolopez.dto.ErrorResponse;
import com.evaluacion.josuesantiagolopez.dto.PaginaResponse;
import com.evaluacion.josuesantiagolopez.dto.ResumenCuentaResponse;
import com.evaluacion.josuesantiagolopez.service.CuentaService;
import io.smallrye.common.annotation.Blocking;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/cuentas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
@Tag(name = "Cuentas", description = "Registro, consulta y resumen de cuentas bancarias")
public class CuentaResource {

    private final CuentaService cuentaService;

    public CuentaResource(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @POST
    @Operation(
            summary = "Registrar una cuenta bancaria",
            description = """
                    Crea una cuenta con número único de 10 dígitos, titular, tipo (AHORRO o CORRIENTE),
                    saldo inicial mayor o igual a cero y estado (ACTIVA o INACTIVA).
                    Rechaza números duplicados y saldos negativos.
                    """)
    @APIResponse(responseCode = "201", description = "Cuenta creada",
            content = @Content(schema = @Schema(implementation = CuentaResponse.class)))
    @APIResponse(responseCode = "400", description = "Datos inválidos o saldo negativo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "409", description = "Ya existe una cuenta con ese número",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response crear(@Valid CuentaRequest solicitud) {
        CuentaResponse creada = cuentaService.registrar(solicitud);
        return Response.status(Response.Status.CREATED).entity(creada).build();
    }

    @GET
    @Operation(
            summary = "Listar cuentas",
            description = "Devuelve las cuentas registradas, paginadas. Se puede ordenar por saldo, titular, número, tipo o estado.")
    @APIResponse(responseCode = "200", description = "Página de cuentas")
    @APIResponse(responseCode = "400", description = "Paginación inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PaginaResponse<CuentaResponse> listar(
            @Parameter(description = "Página, empezando en 0", example = "0")
            @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Tamaño de página, entre 1 y 100", example = "20")
            @QueryParam("size") @DefaultValue("20") int size,
            @Parameter(description = "Campo de orden: id, numeroCuenta, titular, tipo, saldo, estado o fechaCreacion", example = "saldo")
            @QueryParam("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Dirección: asc o desc", example = "desc")
            @QueryParam("direction") @DefaultValue("asc") String direction) {
        return cuentaService.listar(page, size, sort, direction);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Consultar una cuenta por id")
    @APIResponse(responseCode = "200", description = "Cuenta encontrada",
            content = @Content(schema = @Schema(implementation = CuentaResponse.class)))
    @APIResponse(responseCode = "404", description = "La cuenta no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CuentaResponse obtener(
            @Parameter(description = "Identificador de la cuenta", example = "1") @PathParam("id") Long id) {
        return cuentaService.obtener(id);
    }

    @PATCH
    @Path("/{id}")
    @Operation(
            summary = "Activar o inactivar una cuenta",
            description = "Cambia el estado de la cuenta. Una cuenta inactiva no puede originar ni recibir transferencias.")
    @APIResponse(responseCode = "200", description = "Estado actualizado",
            content = @Content(schema = @Schema(implementation = CuentaResponse.class)))
    @APIResponse(responseCode = "400", description = "Estado inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "La cuenta no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CuentaResponse actualizarEstado(
            @Parameter(description = "Identificador de la cuenta", example = "1") @PathParam("id") Long id,
            @Valid ActualizarEstadoRequest solicitud) {
        return cuentaService.actualizarEstado(id, solicitud);
    }

    @GET
    @Path("/{id}/resumen")
    @Operation(
            summary = "Resumen operativo de la cuenta",
            description = """
                    Devuelve el saldo actual, el total transferido hoy, la cantidad de transferencias del día
                    y cuántas de esas transferencias quedaron marcadas como sospechosas.
                    El día calendario usa la zona America/Guayaquil. Solo cuentan las transferencias
                    completadas o en revisión originadas por la cuenta.
                    """)
    @APIResponse(responseCode = "200", description = "Resumen calculado",
            content = @Content(schema = @Schema(implementation = ResumenCuentaResponse.class)))
    @APIResponse(responseCode = "404", description = "La cuenta no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResumenCuentaResponse resumen(
            @Parameter(description = "Identificador de la cuenta", example = "1") @PathParam("id") Long id) {
        return cuentaService.resumen(id);
    }
}
