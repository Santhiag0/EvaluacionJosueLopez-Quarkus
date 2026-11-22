package com.evaluacion.josuelopez.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evaluacion.josuelopez.dominio.ReglasNegocio;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

@ExtendWith(MockitoExtension.class)
class DeteccionSospechosaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Mock
    TransferenciaRepository transferencias;

    DeteccionSospechosaService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-06-15T15:30:00Z"), ZONA);
        servicio = new DeteccionSospechosaService(transferencias, reloj);
    }

    @Test
    void noMarcaCuandoElMontoEsExactamenteCincoMilYLaFrecuenciaEstaDentroDelLimite() {
        when(transferencias.contarExitosasEnVentana(eq(7L), any(), any())).thenReturn(4L);

        ResultadoDeteccion resultado = servicio.evaluar(7L, new BigDecimal("5000.00"));

        assertFalse(resultado.sospechosa());
    }

    @Test
    void marcaCuandoElMontoSuperaCincoMil() {
        when(transferencias.contarExitosasEnVentana(eq(7L), any(), any())).thenReturn(0L);

        ResultadoDeteccion resultado = servicio.evaluar(7L, new BigDecimal("5000.01"));

        assertTrue(resultado.sospechosa());
        assertEquals(ReglasNegocio.MOTIVO_MONTO, resultado.motivo());
    }

    @Test
    void marcaLaSextaTransferenciaDeLaUltimaHora() {
        when(transferencias.contarExitosasEnVentana(eq(7L), any(), any())).thenReturn(5L);

        ResultadoDeteccion resultado = servicio.evaluar(7L, new BigDecimal("10.00"));

        assertTrue(resultado.sospechosa());
        assertEquals(ReglasNegocio.MOTIVO_FRECUENCIA, resultado.motivo());
    }

    @Test
    void noMarcaCuandoHayExactamenteCuatroTransferenciasPrevias() {
        when(transferencias.contarExitosasEnVentana(eq(7L), any(), any())).thenReturn(4L);

        ResultadoDeteccion resultado = servicio.evaluar(7L, new BigDecimal("10.00"));

        assertFalse(resultado.sospechosa());
    }

    @Test
    void laVentanaEsLaUltimaHoraRodante() {
        when(transferencias.contarExitosasEnVentana(eq(7L), any(), any())).thenReturn(0L);

        servicio.evaluar(7L, new BigDecimal("10.00"));

        ArgumentCaptor<LocalDateTime> desde = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hasta = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(transferencias).contarExitosasEnVentana(eq(7L), desde.capture(), hasta.capture());
        assertEquals(LocalDateTime.of(2026, 6, 15, 9, 30), desde.getValue());
        assertEquals(LocalDateTime.of(2026, 6, 15, 10, 30), hasta.getValue());
    }

    @Test
    void juntaLosDosMotivosCuandoAmbosSeCumplen() {
        when(transferencias.contarExitosasEnVentana(eq(7L), any(), any())).thenReturn(6L);

        ResultadoDeteccion resultado = servicio.evaluar(7L, new BigDecimal("8000.00"));

        assertTrue(resultado.sospechosa());
        assertTrue(resultado.motivo().contains(ReglasNegocio.MOTIVO_MONTO));
        assertTrue(resultado.motivo().contains(ReglasNegocio.MOTIVO_FRECUENCIA));
    }
}
