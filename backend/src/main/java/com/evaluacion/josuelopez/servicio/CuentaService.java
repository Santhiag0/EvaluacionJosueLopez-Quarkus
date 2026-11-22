package com.evaluacion.josuelopez.servicio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.evaluacion.josuelopez.dominio.Cuenta;
import com.evaluacion.josuelopez.dto.CuentaRequest;
import com.evaluacion.josuelopez.dto.CuentaResponse;
import com.evaluacion.josuelopez.dto.CuentaResumenResponse;
import com.evaluacion.josuelopez.dto.PaginaResponse;
import com.evaluacion.josuelopez.excepcion.ReglaNegocioException;
import com.evaluacion.josuelopez.mapeador.CuentaMapper;
import com.evaluacion.josuelopez.repositorio.CuentaRepository;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class CuentaService {

    private static final Logger log = LoggerFactory.getLogger(CuentaService.class);

    private static final Map<String, String> CAMPOS_ORDEN = Map.of(
            "numeroCuenta", "numeroCuenta",
            "titular", "titular",
            "tipo", "tipo",
            "saldo", "saldo",
            "estado", "estado",
            "fechaCreacion", "fechaCreacion");

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final Clock reloj;

    @Inject
    public CuentaService(CuentaRepository cuentaRepository, TransferenciaRepository transferenciaRepository, Clock reloj) {
        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.reloj = reloj;
    }

    @Transactional
    public boolean hayCuentas() {
        return cuentaRepository.count() > 0;
    }

    @Transactional
    public CuentaResponse crear(CuentaRequest solicitud) {
        if (cuentaRepository.existeNumero(solicitud.numeroCuenta())) {
            throw new ReglaNegocioException(409,
                    "Ya existe una cuenta con el número " + solicitud.numeroCuenta());
        }
        Cuenta cuenta = CuentaMapper.aEntidad(solicitud, LocalDateTime.now(reloj));
        cuentaRepository.persist(cuenta);
        log.info("Cuenta creada id={} numero={} titular={} tipo={} saldo={} estado={}",
                cuenta.id, cuenta.numeroCuenta, cuenta.titular, cuenta.tipo, cuenta.saldo, cuenta.estado);
        return CuentaMapper.aRespuesta(cuenta);
    }

    @Transactional
    public PaginaResponse<CuentaResponse> listar(int pagina, int tamano, String ordenarPor, String direccion) {
        String campo = CAMPOS_ORDEN.get(ordenarPor);
        if (campo == null) {
            throw new ReglaNegocioException(400,
                    "El ordenamiento debe ser numeroCuenta, titular, tipo, saldo, estado o fechaCreacion");
        }
        Sort.Direction sentido = "desc".equalsIgnoreCase(direccion)
                ? Sort.Direction.Descending
                : Sort.Direction.Ascending;
        if (!"asc".equalsIgnoreCase(direccion) && !"desc".equalsIgnoreCase(direccion)) {
            throw new ReglaNegocioException(400, "La dirección de ordenamiento debe ser asc o desc");
        }
        var consulta = cuentaRepository.findAll(Sort.by(campo, sentido));
        List<CuentaResponse> contenido = consulta.page(Page.of(pagina, tamano)).list().stream()
                .map(CuentaMapper::aRespuesta)
                .toList();
        return PaginaResponse.de(contenido, pagina, tamano, consulta.count());
    }

    @Transactional
    public CuentaResumenResponse resumen(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id);
        if (cuenta == null) {
            throw new ReglaNegocioException(404, "No existe la cuenta con id " + id);
        }
        LocalDate hoy = LocalDate.now(reloj);
        LocalDateTime desde = hoy.atStartOfDay();
        LocalDateTime hasta = hoy.plusDays(1).atStartOfDay();
        return new CuentaResumenResponse(
                cuenta.id,
                cuenta.numeroCuenta,
                cuenta.titular,
                CuentaMapper.dinero(cuenta.saldo),
                CuentaMapper.dinero(transferenciaRepository.sumarMontoExitoso(cuenta.id, desde, hasta)),
                transferenciaRepository.contarExitosasEnVentana(cuenta.id, desde, hasta),
                transferenciaRepository.contarSospechosas(cuenta.id));
    }
}
