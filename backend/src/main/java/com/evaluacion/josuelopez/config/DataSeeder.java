package com.evaluacion.josuelopez.config;

import java.math.BigDecimal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import com.evaluacion.josuelopez.dominio.EstadoCuenta;
import com.evaluacion.josuelopez.dominio.TipoCuenta;
import com.evaluacion.josuelopez.dto.CuentaRequest;
import com.evaluacion.josuelopez.dto.CuentaResponse;
import com.evaluacion.josuelopez.dto.TransferenciaRequest;
import com.evaluacion.josuelopez.servicio.CuentaService;
import com.evaluacion.josuelopez.servicio.TransferenciaService;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @ConfigProperty(name = "app.seed.enabled", defaultValue = "false")
    boolean habilitada;

    @Inject
    CuentaService cuentaService;

    @Inject
    TransferenciaService transferenciaService;

    void alIniciar(@Observes StartupEvent evento) {
        if (!habilitada || cuentaService.hayCuentas()) {
            return;
        }
        log.info("Cargando cuentas de demostración");
        CuentaResponse ana = crear("1000000001", "Ana María Restrepo", TipoCuenta.AHORRO, "25000.00", EstadoCuenta.ACTIVA);
        CuentaResponse carlos = crear("1000000002", "Carlos Méndez", TipoCuenta.CORRIENTE, "8200.50", EstadoCuenta.ACTIVA);
        crear("1000000003", "Lucía Fernández", TipoCuenta.AHORRO, "1500.00", EstadoCuenta.ACTIVA);
        crear("1000000004", "Andrés Calderón", TipoCuenta.CORRIENTE, "48000.00", EstadoCuenta.ACTIVA);
        crear("1000000005", "Marina Ortiz", TipoCuenta.AHORRO, "730.25", EstadoCuenta.ACTIVA);
        crear("1000000006", "Pedro Salgado", TipoCuenta.CORRIENTE, "300.00", EstadoCuenta.INACTIVA);
        transferenciaService.ejecutar(new TransferenciaRequest(ana.id(), carlos.id(), new BigDecimal("250.00")));
        log.info("Demostración lista: 6 cuentas y una transferencia de $250.00");
    }

    private CuentaResponse crear(String numero, String titular, TipoCuenta tipo, String saldo, EstadoCuenta estado) {
        return cuentaService.crear(new CuentaRequest(numero, titular, tipo, new BigDecimal(saldo), estado));
    }
}
