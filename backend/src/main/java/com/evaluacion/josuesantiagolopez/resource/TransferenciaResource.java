package com.evaluacion.josuesantiagolopez.resource;

import com.evaluacion.josuesantiagolopez.domain.EstadoTransferencia;
import com.evaluacion.josuesantiagolopez.dto.ErrorResponse;
import com.evaluacion.josuesantiagolopez.dto.PaginaResponse;
import com.evaluacion.josuesantiagolopez.dto.TransferenciaRequest;
import com.evaluacion.josuesantiagolopez.dto.TransferenciaResponse;
import com.evaluacion.josuesantiagolopez.exception.BusinessException;
import com.evaluacion.josuesantiagolopez.service.FiltroTransferencia;
import com.evaluacion.josuesantiagolopez.service.TransferenciaService;
import com.evaluacion.josuesantiagolopez.util.FechasConsulta;
import io.smallrye.common.annotation.Blocking;
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
import java.time.ZoneId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/transferencias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
@Tag(name = "Transferencias", description = "Ejecución y consulta del historial de transferencias")
public class TransferenciaResource {

    private final TransferenciaService transferenciaService;
    private final ZoneId zona;

    public TransferenciaResource(
            TransferenciaService transferenciaService,
            @ConfigProperty(name = "app.zona-horaria", defaultValue = "America/Guayaquil") String zona) {
        this.transferenciaService = transferenciaService;
        this.zona = ZoneId.of(zona);
    }

    @POST
    @Operation(
            summary = "Ejecutar una transferencia",
            description = """
                    Transfiere un monto desde la cuenta origen hacia la cuenta destino.
                    La operación es atómica: o se actualizan ambos saldos, o no se actualiza ninguno.
                    Se rechaza si alguna cuenta no existe o está inactiva, si el monto no es mayor a cero,
                    si el origen no tiene saldo o si el acumulado del día superaría $10,000.00.
                    Si el monto supera $5,000.00 o el origen ya hizo más de 5 transferencias en la última hora,
                    la transferencia se completa y queda en estado EN_REVISION.
                    """)
    @APIResponse(responseCode = "201", description = "Transferencia completada o marcada en revisión",
            content = @Content(schema = @Schema(implementation = TransferenciaResponse.class)))
    @APIResponse(responseCode = "400", description = "Monto inválido o las dos cuentas son la misma",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Alguna de las cuentas no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "422", description = "Regla de negocio no cumplida: cuenta inactiva, saldo insuficiente o límite diario",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response crear(@Valid TransferenciaRequest solicitud) {
        TransferenciaResponse creada = transferenciaService.ejecutar(solicitud);
        return Response.status(Response.Status.CREATED).entity(creada).build();
    }

    @GET
    @Operation(
            summary = "Consultar el historial de transferencias",
            description = """
                    Lista las transferencias de la más reciente a la más antigua.
                    Filtros opcionales: cuenta origen, cuenta destino, estado
                    (COMPLETADA, RECHAZADA, EN_REVISION) y rango de fechas.
                    Una fecha yyyy-MM-dd cubre ese día completo en America/Guayaquil.
                    """)
    @APIResponse(responseCode = "200", description = "Página de transferencias ordenada por fecha descendente")
    @APIResponse(responseCode = "400", description = "Filtro o paginación inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PaginaResponse<TransferenciaResponse> listar(
            @Parameter(description = "Id de la cuenta origen", example = "1")
            @QueryParam("cuentaOrigenId") Long cuentaOrigenId,
            @Parameter(description = "Id de la cuenta destino", example = "2")
            @QueryParam("cuentaDestinoId") Long cuentaDestinoId,
            @Parameter(description = "COMPLETADA, RECHAZADA o EN_REVISION. También se acepta 'en revisión'", example = "COMPLETADA")
            @QueryParam("estado") String estado,
            @Parameter(description = "Fecha inicial inclusive, yyyy-MM-dd o instante ISO-8601", example = "2026-11-20")
            @QueryParam("fechaDesde") String fechaDesde,
            @Parameter(description = "Fecha final inclusive si es yyyy-MM-dd, o instante ISO-8601 exclusivo", example = "2026-11-20")
            @QueryParam("fechaHasta") String fechaHasta,
            @Parameter(description = "Página, empezando en 0", example = "0")
            @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Tamaño de página, entre 1 y 100", example = "20")
            @QueryParam("size") @DefaultValue("20") int size) {
        FiltroTransferencia filtro = new FiltroTransferencia();
        filtro.cuentaOrigenId = cuentaOrigenId;
        filtro.cuentaDestinoId = cuentaDestinoId;
        filtro.estado = parsearEstado(estado);
        filtro.fechaDesde = FechasConsulta.inicio(fechaDesde, zona);
        filtro.fechaHastaExclusivo = FechasConsulta.finExclusivo(fechaHasta, zona);
        return transferenciaService.listar(filtro, page, size);
    }

    private EstadoTransferencia parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoTransferencia.desde(estado);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "ESTADO_INVALIDO", ex.getMessage());
        }
    }
}
