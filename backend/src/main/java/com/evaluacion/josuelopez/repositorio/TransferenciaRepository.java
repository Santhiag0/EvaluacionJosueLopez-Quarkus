package com.evaluacion.josuelopez.repositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import com.evaluacion.josuelopez.dominio.EstadoTransferencia;
import com.evaluacion.josuelopez.dominio.Transferencia;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class TransferenciaRepository implements PanacheRepository<Transferencia> {

    private static final List<EstadoTransferencia> ESTADOS_EXITOSOS = List.of(
            EstadoTransferencia.COMPLETADA,
            EstadoTransferencia.EN_REVISION);

    public BigDecimal sumarMontoExitoso(Long cuentaOrigenId, LocalDateTime desde, LocalDateTime hasta) {
        Object total = getEntityManager().createQuery(
                "select coalesce(sum(t.monto), 0) from Transferencia t "
                        + "where t.cuentaOrigen.id = :cuentaId "
                        + "and t.estado in :estados "
                        + "and t.fecha >= :desde and t.fecha < :hasta")
                .setParameter("cuentaId", cuentaOrigenId)
                .setParameter("estados", ESTADOS_EXITOSOS)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();
        if (total == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(total.toString());
    }

    public long contarExitosasEnVentana(Long cuentaOrigenId, LocalDateTime desde, LocalDateTime hasta) {
        Long total = getEntityManager().createQuery(
                "select count(t) from Transferencia t "
                        + "where t.cuentaOrigen.id = :cuentaId "
                        + "and t.estado in :estados "
                        + "and t.fecha >= :desde and t.fecha < :hasta",
                Long.class)
                .setParameter("cuentaId", cuentaOrigenId)
                .setParameter("estados", ESTADOS_EXITOSOS)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();
        return total == null ? 0L : total;
    }

    public long contarSospechosas(Long cuentaOrigenId) {
        return count("cuentaOrigen.id = ?1 and sospechosa = true", cuentaOrigenId);
    }

    public List<Transferencia> buscar(FiltroTransferencia filtro, int pagina, int tamano) {
        Consulta consulta = consulta(filtro);
        var query = getEntityManager().createQuery(
                "select distinct t from Transferencia t "
                        + "join fetch t.cuentaOrigen "
                        + "join fetch t.cuentaDestino "
                        + consulta.where()
                        + " order by t.fecha desc, t.id desc",
                Transferencia.class);
        consulta.parametros().forEach(query::setParameter);
        query.setFirstResult(Math.max(pagina, 0) * tamano);
        query.setMaxResults(tamano);
        return query.getResultList();
    }

    public long contar(FiltroTransferencia filtro) {
        Consulta consulta = consulta(filtro);
        var query = getEntityManager().createQuery(
                "select count(t) from Transferencia t " + consulta.where(),
                Long.class);
        consulta.parametros().forEach(query::setParameter);
        Long total = query.getSingleResult();
        return total == null ? 0L : total;
    }

    private Consulta consulta(FiltroTransferencia filtro) {
        StringBuilder where = new StringBuilder("where 1 = 1");
        Map<String, Object> parametros = new HashMap<>();
        if (filtro.cuentaOrigenId() != null) {
            where.append(" and t.cuentaOrigen.id = :origenId");
            parametros.put("origenId", filtro.cuentaOrigenId());
        }
        if (filtro.cuentaDestinoId() != null) {
            where.append(" and t.cuentaDestino.id = :destinoId");
            parametros.put("destinoId", filtro.cuentaDestinoId());
        }
        if (filtro.estado() != null) {
            where.append(" and t.estado = :estado");
            parametros.put("estado", filtro.estado());
        }
        if (filtro.fechaDesdeInclusive() != null) {
            where.append(" and t.fecha >= :desde");
            parametros.put("desde", filtro.fechaDesdeInclusive());
        }
        if (filtro.fechaHastaExclusiva() != null) {
            where.append(" and t.fecha < :hasta");
            parametros.put("hasta", filtro.fechaHastaExclusiva());
        }
        return new Consulta(where.toString(), parametros);
    }

    private record Consulta(String where, Map<String, Object> parametros) {
    }
}
