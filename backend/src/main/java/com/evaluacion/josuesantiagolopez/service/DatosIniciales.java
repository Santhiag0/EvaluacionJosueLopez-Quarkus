package com.evaluacion.josuesantiagolopez.service;

import com.evaluacion.josuesantiagolopez.domain.Cuenta;
import com.evaluacion.josuesantiagolopez.domain.EstadoCuenta;
import com.evaluacion.josuesantiagolopez.domain.TipoCuenta;
import com.evaluacion.josuesantiagolopez.repository.CuentaRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DatosIniciales {

    private static final Logger LOG = Logger.getLogger(DatosIniciales.class.getName());

    private final CuentaRepository cuentas;
    private final boolean semillaActiva;

    public DatosIniciales(
            CuentaRepository cuentas,
            @ConfigProperty(name = "app.seed.enabled", defaultValue = "true") boolean semillaActiva) {
        this.cuentas = cuentas;
        this.semillaActiva = semillaActiva;
    }

    @Transactional
    void alIniciar(@Observes StartupEvent event) {
        if (!semillaActiva || cuentas.count() > 0) {
            return;
        }
        crear("1000000001", "Ana Pérez", TipoCuenta.AHORRO, "20000.00", EstadoCuenta.ACTIVA);
        crear("1000000002", "Luis Gómez", TipoCuenta.CORRIENTE, "8000.00", EstadoCuenta.ACTIVA);
        crear("1000000003", "María Ruiz", TipoCuenta.AHORRO, "1500.00", EstadoCuenta.INACTIVA);
        crear("1000000004", "Carlos Vega", TipoCuenta.CORRIENTE, "500.00", EstadoCuenta.ACTIVA);
        crear("1000000005", "Josue Santiago Lopez", TipoCuenta.AHORRO, "15000.00", EstadoCuenta.ACTIVA);
        LOG.info("semilla_cuentas cantidad=5");
    }

    private void crear(String numero, String titular, TipoCuenta tipo, String saldo, EstadoCuenta estado) {
        Cuenta cuenta = new Cuenta();
        cuenta.numeroCuenta = numero;
        cuenta.titular = titular;
        cuenta.tipo = tipo;
        cuenta.saldo = new BigDecimal(saldo);
        cuenta.estado = estado;
        cuenta.fechaCreacion = Instant.now();
        cuentas.persist(cuenta);
    }
}
