package com.evaluacion.josuelopez.servicio;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evaluacion.josuelopez.dominio.EstadoTransferencia;
import com.evaluacion.josuelopez.dto.CuentaOperacionResponse;
import com.evaluacion.josuelopez.dto.TransferenciaRequest;
import com.evaluacion.josuelopez.dto.TransferenciaResponse;
import com.evaluacion.josuelopez.excepcion.ReglaNegocioException;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    TransaccionTransferencia transaccion;

    @Mock
    RechazoTransferenciaService rechazos;

    @Mock
    TransferenciaRepository transferencias;

    @InjectMocks
    TransferenciaService servicio;

    @Test
    void guardaElRechazoDespuesDeQueLaTransaccionDeNegocioFalla() {
        TransferenciaRequest solicitud = new TransferenciaRequest(1L, 2L, new BigDecimal("80.00"));
        when(transaccion.ejecutar(solicitud))
                .thenThrow(new ReglaNegocioException(422, "Saldo insuficiente", 1L, 2L, true));

        assertThrows(ReglaNegocioException.class, () -> servicio.ejecutar(solicitud));

        verify(rechazos).registrar(1L, 2L, new BigDecimal("80.00"), "Saldo insuficiente");
    }

    @Test
    void noGuardaRechazoCuandoLaCuentaNoExiste() {
        TransferenciaRequest solicitud = new TransferenciaRequest(1L, 9L, new BigDecimal("80.00"));
        when(transaccion.ejecutar(solicitud))
                .thenThrow(new ReglaNegocioException(404, "No existe la cuenta destino con id 9"));

        assertThrows(ReglaNegocioException.class, () -> servicio.ejecutar(solicitud));

        verify(rechazos, never()).registrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void devuelveLaRespuestaCuandoLaTransferenciaSeRegistra() {
        TransferenciaRequest solicitud = new TransferenciaRequest(1L, 2L, new BigDecimal("80.00"));
        TransferenciaResponse respuesta = new TransferenciaResponse(
                5L,
                new CuentaOperacionResponse(1L, "1000000001", "Ana", new BigDecimal("10.00")),
                new CuentaOperacionResponse(2L, "1000000002", "Luis", new BigDecimal("90.00")),
                new BigDecimal("80.00"),
                null,
                EstadoTransferencia.COMPLETADA,
                false,
                null);
        when(transaccion.ejecutar(solicitud)).thenReturn(respuesta);

        assertSame(respuesta, servicio.ejecutar(solicitud));
    }
}
