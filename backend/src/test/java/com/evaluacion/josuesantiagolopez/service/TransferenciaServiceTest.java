package com.evaluacion.josuesantiagolopez.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evaluacion.josuesantiagolopez.domain.Cuenta;
import com.evaluacion.josuesantiagolopez.domain.EstadoCuenta;
import com.evaluacion.josuesantiagolopez.domain.EstadoTransferencia;
import com.evaluacion.josuesantiagolopez.domain.Transferencia;
import com.evaluacion.josuesantiagolopez.dto.TransferenciaRequest;
import com.evaluacion.josuesantiagolopez.dto.TransferenciaResponse;
import com.evaluacion.josuesantiagolopez.exception.BusinessException;
import com.evaluacion.josuesantiagolopez.exception.RecursoNoEncontradoException;
import com.evaluacion.josuesantiagolopez.repository.CuentaRepository;
import com.evaluacion.josuesantiagolopez.repository.TransferenciaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    CuentaRepository cuentas;

    @Mock
    TransferenciaRepository transferencias;

    @Mock
    DeteccionSospechosaService deteccion;

    @Mock
    LimiteDiarioService limiteDiario;

    TransferenciaService servicio;

    Cuenta origen;
    Cuenta destino;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-11-20T15:00:00Z"), ZoneOffset.UTC);
        servicio = new TransferenciaService(cuentas, transferencias, deteccion, limiteDiario, reloj);
        origen = cuenta(1L, "1000000001", "1000.00", EstadoCuenta.ACTIVA);
        destino = cuenta(2L, "1000000002", "200.00", EstadoCuenta.ACTIVA);
    }

    @Test
    void transfiereYActualizaAmbosSaldos() {
        prepararCuentas();
        when(deteccion.evaluar(any(), any(), any())).thenReturn(ResultadoDeteccion.limpia());

        TransferenciaResponse respuesta = servicio.ejecutar(solicitud("100.00"));

        assertEquals(EstadoTransferencia.COMPLETADA, respuesta.estado);
        assertFalse(respuesta.sospechosa);
        assertEquals(new BigDecimal("900.00"), origen.saldo);
        assertEquals(new BigDecimal("300.00"), destino.saldo);
        verify(transferencias).persist(any(Transferencia.class));
    }

    @Test
    void marcaEnRevisionCuandoLaDeteccionLoIndica() {
        prepararCuentas();
        when(deteccion.evaluar(any(), any(), any()))
                .thenReturn(new ResultadoDeteccion(true, "El monto supera $5000.00"));
        origen.saldo = new BigDecimal("9000.00");

        TransferenciaResponse respuesta = servicio.ejecutar(solicitud("6000.00"));

        assertEquals(EstadoTransferencia.EN_REVISION, respuesta.estado);
        assertTrue(respuesta.sospechosa);
        assertEquals(new BigDecimal("3000.00"), origen.saldo);
        assertEquals(new BigDecimal("6200.00"), destino.saldo);
    }

    @Test
    void rechazaSinMoverSaldosCuandoNoHaySaldo() {
        prepararCuentas();

        BusinessException error = assertThrows(BusinessException.class, () -> servicio.ejecutar(solicitud("1500.00")));

        assertEquals(422, error.status());
        assertEquals("SALDO_INSUFICIENTE", error.codigo());
        assertTrue(error.getMessage().contains("1000.00"));
        assertEquals(new BigDecimal("1000.00"), origen.saldo);
        assertEquals(new BigDecimal("200.00"), destino.saldo);
        Transferencia guardada = captura();
        assertEquals(EstadoTransferencia.RECHAZADA, guardada.estado);
    }

    @Test
    void rechazaCuentaOrigenInactiva() {
        origen.estado = EstadoCuenta.INACTIVA;
        prepararCuentas();

        BusinessException error = assertThrows(BusinessException.class, () -> servicio.ejecutar(solicitud("10.00")));

        assertEquals(422, error.status());
        assertEquals("CUENTA_ORIGEN_INACTIVA", error.codigo());
        assertEquals(new BigDecimal("1000.00"), origen.saldo);
        assertEquals(EstadoTransferencia.RECHAZADA, captura().estado);
    }

    @Test
    void rechazaCuentaDestinoInactiva() {
        destino.estado = EstadoCuenta.INACTIVA;
        prepararCuentas();

        BusinessException error = assertThrows(BusinessException.class, () -> servicio.ejecutar(solicitud("10.00")));

        assertEquals("CUENTA_DESTINO_INACTIVA", error.codigo());
        assertEquals(new BigDecimal("1000.00"), origen.saldo);
        assertEquals(new BigDecimal("200.00"), destino.saldo);
    }

    @Test
    void noMueveSaldosSiSuperaElLimiteDiario() {
        prepararCuentas();
        org.mockito.Mockito.doThrow(new BusinessException(422, "LIMITE_DIARIO_EXCEDIDO",
                "La transferencia supera el límite diario de $10000.00. Monto restante disponible: $40.00"))
                .when(limiteDiario).validar(any(), any(), any());

        BusinessException error = assertThrows(BusinessException.class, () -> servicio.ejecutar(solicitud("50.00")));

        assertTrue(error.getMessage().contains("40.00"));
        assertEquals(new BigDecimal("1000.00"), origen.saldo);
        assertEquals(new BigDecimal("200.00"), destino.saldo);
        assertEquals(EstadoTransferencia.RECHAZADA, captura().estado);
    }

    @Test
    void responde404SiLaCuentaNoExiste() {
        when(cuentas.findByIdForUpdate(1L)).thenReturn(Optional.of(origen));
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.ejecutar(solicitud("10.00")));
        verify(transferencias, never()).persist(any(Transferencia.class));
    }

    @Test
    void rechazaLaMismaCuentaEnOrigenYDestino() {
        TransferenciaRequest solicitud = solicitud("10.00");
        solicitud.cuentaDestinoId = 1L;

        BusinessException error = assertThrows(BusinessException.class, () -> servicio.ejecutar(solicitud));

        assertEquals(400, error.status());
        verify(cuentas, never()).findByIdForUpdate(any());
    }

    private void prepararCuentas() {
        when(cuentas.findByIdForUpdate(1L)).thenReturn(Optional.of(origen));
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.of(destino));
    }

    private Transferencia captura() {
        ArgumentCaptor<Transferencia> captor = ArgumentCaptor.forClass(Transferencia.class);
        verify(transferencias).persist(captor.capture());
        return captor.getValue();
    }

    private static TransferenciaRequest solicitud(String monto) {
        TransferenciaRequest solicitud = new TransferenciaRequest();
        solicitud.cuentaOrigenId = 1L;
        solicitud.cuentaDestinoId = 2L;
        solicitud.monto = new BigDecimal(monto);
        return solicitud;
    }

    private static Cuenta cuenta(Long id, String numero, String saldo, EstadoCuenta estado) {
        Cuenta cuenta = new Cuenta();
        cuenta.id = id;
        cuenta.numeroCuenta = numero;
        cuenta.titular = "Titular " + numero;
        cuenta.saldo = new BigDecimal(saldo);
        cuenta.estado = estado;
        return cuenta;
    }
}
