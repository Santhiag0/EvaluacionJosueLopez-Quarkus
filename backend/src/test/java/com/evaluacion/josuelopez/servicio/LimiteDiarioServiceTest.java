package com.evaluacion.josuelopez.servicio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.evaluacion.josuelopez.excepcion.ReglaNegocioException;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

@ExtendWith(MockitoExtension.class)
class LimiteDiarioServiceTest {

    @Mock
    TransferenciaRepository transferencias;

    LimiteDiarioService servicio;

    @BeforeEach
    void preparar() {
        // 2026-06-15T04:30:00Z es todavía el 14 de junio en America/Bogota (UTC-5).
        Clock reloj = Clock.fixed(Instant.parse("2026-06-15T04:30:00Z"), ZoneId.of("America/Bogota"));
        servicio = new LimiteDiarioService(transferencias, reloj);
    }

    @Test
    void elDiaCalendarioUsaLaZonaDeNegocio() {
        when(transferencias.sumarMontoExitoso(eq(3L), any(), any())).thenReturn(new BigDecimal("1000.00"));

        servicio.validar(3L, 4L, new BigDecimal("10.00"));

        ArgumentCaptor<LocalDateTime> desde = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hasta = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(transferencias).sumarMontoExitoso(eq(3L), desde.capture(), hasta.capture());
        assertEquals(LocalDateTime.of(2026, 6, 14, 0, 0), desde.getValue());
        assertEquals(LocalDateTime.of(2026, 6, 15, 0, 0), hasta.getValue());
    }

    @Test
    void permiteLlegarExactamenteAlLimite() {
        when(transferencias.sumarMontoExitoso(eq(3L), any(), any())).thenReturn(BigDecimal.ZERO);

        assertDoesNotThrow(() -> servicio.validar(3L, 4L, new BigDecimal("10000.00")));
    }

    @Test
    void rechazaUnCentavoPorEncimaEInformaElRestante() {
        when(transferencias.sumarMontoExitoso(eq(3L), any(), any())).thenReturn(new BigDecimal("6000.00"));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.validar(3L, 4L, new BigDecimal("4000.01")));

        assertEquals(422, error.getStatus());
        assertTrue(error.isRegistrarRechazo());
        assertTrue(error.getMessage().contains("$4000.00"));
    }

    @Test
    void elRestanteEsCeroCuandoElCupoYaSeAgoto() {
        when(transferencias.sumarMontoExitoso(eq(3L), any(), any())).thenReturn(new BigDecimal("10000.00"));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.validar(3L, 4L, new BigDecimal("0.01")));

        assertTrue(error.getMessage().contains("$0.00"));
    }
}
