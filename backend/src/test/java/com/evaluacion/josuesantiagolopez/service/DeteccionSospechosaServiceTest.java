package com.evaluacion.josuesantiagolopez.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.evaluacion.josuesantiagolopez.domain.Cuenta;
import com.evaluacion.josuesantiagolopez.repository.TransferenciaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeteccionSospechosaServiceTest {

    @Mock
    TransferenciaRepository transferencias;

    DeteccionSospechosaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new DeteccionSospechosaService(transferencias);
    }

    @Test
    void montoIgualAlUmbralNoEsSospechoso() {
        ResultadoDeteccion resultado = servicio.evaluar(new BigDecimal("5000.00"), 0);

        assertFalse(resultado.sospechosa());
    }

    @Test
    void montoQueSuperaElUmbralEsSospechoso() {
        ResultadoDeteccion resultado = servicio.evaluar(new BigDecimal("5000.01"), 0);

        assertTrue(resultado.sospechosa());
        assertTrue(resultado.motivo().contains("supera $5000.00"));
    }

    @Test
    void cincoTransferenciasEnLaHoraNoMarcanLaSiguiente() {
        ResultadoDeteccion resultado = servicio.evaluar(new BigDecimal("10.00"), 5);

        assertFalse(resultado.sospechosa());
    }

    @Test
    void masDeCincoTransferenciasEnLaHoraMarcanLaOperacion() {
        ResultadoDeteccion resultado = servicio.evaluar(new BigDecimal("10.00"), 6);

        assertTrue(resultado.sospechosa());
        assertTrue(resultado.motivo().contains("más de 5 transferencias"));
    }

    @Test
    void combinaMontoYFrecuencia() {
        ResultadoDeteccion resultado = servicio.evaluar(new BigDecimal("8000.00"), 7);

        assertTrue(resultado.sospechosa());
        assertTrue(resultado.motivo().contains("supera $5000.00"));
        assertTrue(resultado.motivo().contains("más de 5 transferencias"));
    }

    @Test
    void consultaLasTransferenciasExitosasDeLaUltimaHora() {
        Cuenta origen = new Cuenta();
        origen.id = 15L;
        when(transferencias.contarExitosasDesde(org.mockito.ArgumentMatchers.eq(15L), any()))
                .thenReturn(6L);

        ResultadoDeteccion resultado = servicio.evaluar(origen, new BigDecimal("20.00"), Instant.parse("2026-11-20T18:00:00Z"));

        assertTrue(resultado.sospechosa());
        assertEquals("La cuenta origen acumula más de 5 transferencias en la última hora", resultado.motivo());
    }
}
