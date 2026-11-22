package com.evaluacion.josuelopez.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evaluacion.josuelopez.dominio.Cuenta;
import com.evaluacion.josuelopez.dominio.EstadoCuenta;
import com.evaluacion.josuelopez.dominio.EstadoTransferencia;
import com.evaluacion.josuelopez.dominio.ReglasNegocio;
import com.evaluacion.josuelopez.dominio.Transferencia;
import com.evaluacion.josuelopez.dto.TransferenciaRequest;
import com.evaluacion.josuelopez.dto.TransferenciaResponse;
import com.evaluacion.josuelopez.excepcion.ReglaNegocioException;
import com.evaluacion.josuelopez.repositorio.CuentaRepository;
import com.evaluacion.josuelopez.repositorio.TransferenciaRepository;

@ExtendWith(MockitoExtension.class)
class TransaccionTransferenciaTest {

    @Mock
    CuentaRepository cuentas;

    @Mock
    TransferenciaRepository transferencias;

    @Mock
    DeteccionSospechosaService deteccion;

    @Mock
    LimiteDiarioService limiteDiario;

    TransaccionTransferencia servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-11-22T15:00:00Z"), ZoneId.of("America/Bogota"));
        servicio = new TransaccionTransferencia(cuentas, transferencias, deteccion, limiteDiario, reloj);
    }

    @Test
    void mueveAmbosSaldosYDejaLaTransferenciaCompletada() {
        Cuenta origen = cuenta(1L, new BigDecimal("1000.00"));
        Cuenta destino = cuenta(2L, new BigDecimal("100.00"));
        when(cuentas.bloquearPorId(1L)).thenReturn(origen);
        when(cuentas.bloquearPorId(2L)).thenReturn(destino);
        BigDecimal monto = new BigDecimal("250.00");
        when(deteccion.evaluar(1L, monto)).thenReturn(ResultadoDeteccion.limpia());

        TransferenciaResponse respuesta = servicio.ejecutar(new TransferenciaRequest(1L, 2L, monto));

        assertEquals(EstadoTransferencia.COMPLETADA, respuesta.estado());
        assertFalse(respuesta.sospechosa());
        assertEquals(0, origen.saldo.compareTo(new BigDecimal("750.00")));
        assertEquals(0, destino.saldo.compareTo(new BigDecimal("350.00")));
        verify(transferencias).persist(any(Transferencia.class));
    }

    @Test
    void bloqueaLasCuentasEnOrdenDeIdParaEvitarInterbloqueos() {
        when(cuentas.bloquearPorId(3L)).thenReturn(cuenta(3L, new BigDecimal("50.00")));
        when(cuentas.bloquearPorId(8L)).thenReturn(cuenta(8L, new BigDecimal("50.00")));
        BigDecimal monto = new BigDecimal("10.00");
        when(deteccion.evaluar(8L, monto)).thenReturn(ResultadoDeteccion.limpia());

        servicio.ejecutar(new TransferenciaRequest(8L, 3L, monto));

        InOrder orden = inOrder(cuentas);
        orden.verify(cuentas).bloquearPorId(3L);
        orden.verify(cuentas).bloquearPorId(8L);
    }

    @Test
    void noMueveSaldosCuandoNoHayFondosYPideRegistrarElRechazo() {
        Cuenta origen = cuenta(1L, new BigDecimal("100.00"));
        Cuenta destino = cuenta(2L, new BigDecimal("100.00"));
        when(cuentas.bloquearPorId(1L)).thenReturn(origen);
        when(cuentas.bloquearPorId(2L)).thenReturn(destino);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.ejecutar(new TransferenciaRequest(1L, 2L, new BigDecimal("100.01"))));

        assertEquals(422, error.getStatus());
        assertTrue(error.isRegistrarRechazo());
        assertTrue(error.getMessage().contains("$100.00"));
        assertEquals(0, origen.saldo.compareTo(new BigDecimal("100.00")));
        verify(transferencias, never()).persist(any(Transferencia.class));
    }

    @Test
    void rechazaCuentaInactivaSinMoverSaldo() {
        Cuenta origen = cuenta(1L, new BigDecimal("500.00"));
        origen.estado = EstadoCuenta.INACTIVA;
        when(cuentas.bloquearPorId(1L)).thenReturn(origen);
        when(cuentas.bloquearPorId(2L)).thenReturn(cuenta(2L, new BigDecimal("500.00")));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.ejecutar(new TransferenciaRequest(1L, 2L, new BigDecimal("10.00"))));

        assertEquals(422, error.getStatus());
        assertTrue(error.getMessage().contains("inactiva"));
        verify(transferencias, never()).persist(any(Transferencia.class));
    }

    @Test
    void responde404CuandoLaCuentaNoExiste() {
        when(cuentas.bloquearPorId(2L)).thenReturn(cuenta(2L, new BigDecimal("500.00")));
        when(cuentas.bloquearPorId(9L)).thenReturn(null);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.ejecutar(new TransferenciaRequest(9L, 2L, new BigDecimal("10.00"))));

        assertEquals(404, error.getStatus());
        assertFalse(error.isRegistrarRechazo());
    }

    @Test
    void rechazaTransferenciaALaMismaCuentaAntesDeBloquear() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.ejecutar(new TransferenciaRequest(1L, 1L, new BigDecimal("10.00"))));

        assertEquals(422, error.getStatus());
        verify(cuentas, never()).bloquearPorId(anyLong());
    }

    @Test
    void unaOperacionSospechosaQuedaEnRevisionYSiMueveElDinero() {
        Cuenta origen = cuenta(1L, new BigDecimal("9000.00"));
        Cuenta destino = cuenta(2L, new BigDecimal("0.00"));
        when(cuentas.bloquearPorId(1L)).thenReturn(origen);
        when(cuentas.bloquearPorId(2L)).thenReturn(destino);
        BigDecimal monto = new BigDecimal("6000.00");
        when(deteccion.evaluar(1L, monto)).thenReturn(new ResultadoDeteccion(true, ReglasNegocio.MOTIVO_MONTO));

        servicio.ejecutar(new TransferenciaRequest(1L, 2L, monto));

        ArgumentCaptor<Transferencia> captor = ArgumentCaptor.forClass(Transferencia.class);
        verify(transferencias).persist(captor.capture());
        assertEquals(EstadoTransferencia.EN_REVISION, captor.getValue().estado);
        assertTrue(captor.getValue().sospechosa);
        assertEquals(0, origen.saldo.compareTo(new BigDecimal("3000.00")));
        assertEquals(0, destino.saldo.compareTo(new BigDecimal("6000.00")));
    }

    @Test
    void elLimiteDiarioImpideActualizarLosSaldos() {
        Cuenta origen = cuenta(1L, new BigDecimal("20000.00"));
        when(cuentas.bloquearPorId(1L)).thenReturn(origen);
        when(cuentas.bloquearPorId(2L)).thenReturn(cuenta(2L, new BigDecimal("0.00")));
        BigDecimal monto = new BigDecimal("100.00");
        doThrow(new ReglaNegocioException(422, "limite", 1L, 2L, true))
                .when(limiteDiario).validar(1L, 2L, monto);

        assertThrows(ReglaNegocioException.class,
                () -> servicio.ejecutar(new TransferenciaRequest(1L, 2L, monto)));

        assertEquals(0, origen.saldo.compareTo(new BigDecimal("20000.00")));
        verify(transferencias, never()).persist(any(Transferencia.class));
    }

    private static Cuenta cuenta(Long id, BigDecimal saldo) {
        Cuenta cuenta = new Cuenta();
        cuenta.id = id;
        cuenta.numeroCuenta = String.format("%010d", id);
        cuenta.titular = "Titular " + id;
        cuenta.saldo = saldo;
        cuenta.estado = EstadoCuenta.ACTIVA;
        return cuenta;
    }
}
