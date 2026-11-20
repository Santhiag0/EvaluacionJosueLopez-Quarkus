package com.evaluacion.josuesantiagolopez.repository;

import com.evaluacion.josuesantiagolopez.domain.EstadoTransferencia;
import com.evaluacion.josuesantiagolopez.domain.Transferencia;
import com.evaluacion.josuesantiagolopez.service.FiltroTransferencia;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TransferenciaRepository implements PanacheRepository<Transferencia> {

    private static final List<EstadoTransferencia> EXITOSAS = List.of(
            EstadoTransferencia.COMPLETADA,
            EstadoTransferencia.EN_REVISION);

    public long contarExitosasDesde(Long cuentaOrigenId, Instant desde) {
        return count("cuentaOrigen.id = ?1 and fecha >= ?2 and estado in ?3",
                cuentaOrigenId, desde, EXITOSAS);
    }

    public BigDecimal sumarExitosas(Long cuentaOrigenId, Instant desde, Instant hastaExclusivo) {
        Object total = getEntityManager()
                .createQuery("""
                        select coalesce(sum(t.monto), 0)
                        from Transferencia t
                        where t.cuentaOrigen.id = :id
                          and t.fecha >= :desde
                          and t.fecha < :hasta
                          and t.estado in :estados
                        """)
                .setParameter("id", cuentaOrigenId)
                .setParameter("desde", desde)
                .setParameter("hasta", hastaExclusivo)
                .setParameter("estados", EXITOSAS)
                .getSingleResult();
        if (total == null) {
            return BigDecimal.ZERO;
        }
        if (total instanceof BigDecimal monto) {
            return monto;
        }
        return new BigDecimal(total.toString());
    }

    public long contarExitosas(Long cuentaOrigenId, Instant desde, Instant hastaExclusivo) {
        return count("cuentaOrigen.id = ?1 and fecha >= ?2 and fecha < ?3 and estado in ?4",
                cuentaOrigenId, desde, hastaExclusivo, EXITOSAS);
    }

    public long contarSospechosas(Long cuentaOrigenId, Instant desde, Instant hastaExclusivo) {
        return count("""
                cuentaOrigen.id = ?1 and fecha >= ?2 and fecha < ?3 and sospechosa = true
                and estado <> ?4
                """,
                cuentaOrigenId, desde, hastaExclusivo, EstadoTransferencia.RECHAZADA);
    }

    public PanacheQuery<Transferencia> buscar(FiltroTransferencia filtro) {
        StringBuilder consulta = new StringBuilder("1 = 1");
        Map<String, Object> parametros = new HashMap<>();
        if (filtro.cuentaOrigenId != null) {
            consulta.append(" and cuentaOrigen.id = :origen");
            parametros.put("origen", filtro.cuentaOrigenId);
        }
        if (filtro.cuentaDestinoId != null) {
            consulta.append(" and cuentaDestino.id = :destino");
            parametros.put("destino", filtro.cuentaDestinoId);
        }
        if (filtro.estado != null) {
            consulta.append(" and estado = :estado");
            parametros.put("estado", filtro.estado);
        }
        if (filtro.fechaDesde != null) {
            consulta.append(" and fecha >= :desde");
            parametros.put("desde", filtro.fechaDesde);
        }
        if (filtro.fechaHastaExclusivo != null) {
            consulta.append(" and fecha < :hasta");
            parametros.put("hasta", filtro.fechaHastaExclusivo);
        }
        consulta.append(" order by fecha desc, id desc");
        return find(consulta.toString(), parametros);
    }
}
