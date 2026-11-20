package com.evaluacion.josuesantiagolopez.service;

import com.evaluacion.josuesantiagolopez.domain.Cuenta;
import com.evaluacion.josuesantiagolopez.dto.ActualizarEstadoRequest;
import com.evaluacion.josuesantiagolopez.dto.CuentaRequest;
import com.evaluacion.josuesantiagolopez.dto.CuentaResponse;
import com.evaluacion.josuesantiagolopez.dto.PaginaResponse;
import com.evaluacion.josuesantiagolopez.dto.ResumenCuentaResponse;
import com.evaluacion.josuesantiagolopez.exception.BusinessException;
import com.evaluacion.josuesantiagolopez.exception.RecursoNoEncontradoException;
import com.evaluacion.josuesantiagolopez.mapper.CuentaMapper;
import com.evaluacion.josuesantiagolopez.repository.CuentaRepository;
import com.evaluacion.josuesantiagolopez.repository.TransferenciaRepository;
import com.evaluacion.josuesantiagolopez.util.Montos;
import com.evaluacion.josuesantiagolopez.util.Paginacion;
import com.evaluacion.josuesantiagolopez.util.RangoDia;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CuentaService {

    private static final Set<String> CAMPOS_ORDEN = Set.of(
            "id", "numeroCuenta", "titular", "tipo", "saldo", "estado", "fechaCreacion");

    private final CuentaRepository cuentas;
    private final TransferenciaRepository transferencias;
    private final Clock reloj;
    private final ZoneId zona;

    public CuentaService(
            CuentaRepository cuentas,
            TransferenciaRepository transferencias,
            Clock reloj,
            @ConfigProperty(name = "app.zona-horaria", defaultValue = "America/Guayaquil") String zona) {
        this.cuentas = cuentas;
        this.transferencias = transferencias;
        this.reloj = reloj;
        this.zona = ZoneId.of(zona);
    }

    @Transactional
    public CuentaResponse registrar(CuentaRequest solicitud) {
        String numero = solicitud.numeroCuenta.trim();
        if (cuentas.findByNumeroCuenta(numero).isPresent()) {
            throw new BusinessException(409, "CUENTA_DUPLICADA",
                    "Ya existe una cuenta con el número " + numero);
        }
        if (solicitud.saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "SALDO_NEGATIVO", "El saldo inicial no puede ser negativo");
        }

        Cuenta cuenta = new Cuenta();
        cuenta.numeroCuenta = numero;
        cuenta.titular = solicitud.titular.trim();
        cuenta.tipo = solicitud.tipo;
        cuenta.saldo = Montos.aEscala(solicitud.saldoInicial);
        cuenta.estado = solicitud.estado;
        cuenta.fechaCreacion = Instant.now(reloj);
        cuentas.persist(cuenta);
        return CuentaMapper.toResponse(cuenta);
    }

    @Transactional
    public PaginaResponse<CuentaResponse> listar(int page, int size, String sort, String direction) {
        Paginacion.validar(page, size);
        String campo = CAMPOS_ORDEN.contains(sort) ? sort : "id";
        String direccion = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";
        PanacheQuery<Cuenta> consulta = cuentas.find("order by " + campo + " " + direccion)
                .page(Page.of(page, size));
        List<CuentaResponse> contenido = consulta.list().stream().map(CuentaMapper::toResponse).toList();
        return PaginaResponse.of(contenido, page, size, consulta.count());
    }

    @Transactional
    public CuentaResponse obtener(Long id) {
        return CuentaMapper.toResponse(buscar(id));
    }

    @Transactional
    public CuentaResponse actualizarEstado(Long id, ActualizarEstadoRequest solicitud) {
        Cuenta cuenta = buscar(id);
        cuenta.estado = solicitud.estado;
        return CuentaMapper.toResponse(cuenta);
    }

    @Transactional
    public ResumenCuentaResponse resumen(Long id) {
        Cuenta cuenta = buscar(id);
        RangoDia rango = RangoDia.desde(Instant.now(reloj), zona);
        ResumenCuentaResponse resumen = new ResumenCuentaResponse();
        resumen.cuentaId = cuenta.id;
        resumen.numeroCuenta = cuenta.numeroCuenta;
        resumen.titular = cuenta.titular;
        resumen.saldoActual = cuenta.saldo;
        resumen.totalTransferidoHoy = transferencias.sumarExitosas(cuenta.id, rango.inicio(), rango.finExclusivo());
        resumen.cantidadTransferenciasDelDia = transferencias.contarExitosas(
                cuenta.id, rango.inicio(), rango.finExclusivo());
        resumen.cantidadMarcadasComoSospechosas = transferencias.contarSospechosas(
                cuenta.id, rango.inicio(), rango.finExclusivo());
        return resumen;
    }

    private Cuenta buscar(Long id) {
        return cuentas.findByIdOptional(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta con id " + id));
    }
}
