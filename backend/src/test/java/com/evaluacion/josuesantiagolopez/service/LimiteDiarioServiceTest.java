package com.evaluacion.josuesantiagolopez.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.evaluacion.josuesantiagolopez.exception.BusinessException;
import com.evaluacion.josuesantiagolopez.repository.TransferenciaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LimiteDiarioServiceTest {

    @Mock
    TransferenciaRepository transferencias;

    LimiteDiarioService servicio;

    @BeforeEach
    void preparar() {
        servicio = new LimiteDiarioService(transferencias, "America/Guayaquil");
    }

    @Test
    void permiteLlegarExactamenteAlLimite() {
        when(transferencias.sumarExitosas(eq(4L), any(), any())).thenReturn(new BigDecimal("9000.00"));

        assertDoesNotThrow(() -> servicio.validar(4L, new BigDecimal("1000.00"), Instant.parse("2026-11-20T15:00:00Z")));
    }

    @Test
    void rechazaCuandoElAcumuladoSuperariaElLimiteEInformaElRestante() {
        when(transferencias.sumarExitosas(eq(4L), any(), any())).thenReturn(new BigDecimal("7500.00"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> servicio.validar(4L, new BigDecimal("3000.00"), Instant.parse("2026-11-20T15:00:00Z")));

        assertEqualsStatus(422, error);
        assertTrue(error.getMessage().contains("límite diario"));
        assertTrue(error.getMessage().contains("2500.00"));
    }

    @Test
    void informaRestanteCeroCuandoElCupoYaSeAgoto() {
        when(transferencias.sumarExitosas(eq(4L), any(), any())).thenReturn(new BigDecimal("10000.00"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> servicio.validar(4L, new BigDecimal("0.01"), Instant.parse("2026-11-20T15:00:00Z")));

        assertTrue(error.getMessage().contains("Monto restante disponible: $0.00"));
    }

    private static void assertEqualsStatus(int esperado, BusinessException error) {
        org.junit.jupiter.api.Assertions.assertEquals(esperado, error.status());
    }
}
