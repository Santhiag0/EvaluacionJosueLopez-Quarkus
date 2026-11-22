package com.evaluacion.josuelopez.api;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class TransferenciasTest {

    private static final AtomicLong SECUENCIA = new AtomicLong(8_000_000_000L);

    @Test
    void registraListaYRechazaCuentasInvalidas() {
        String numero = numero();
        Response creada = crearCuenta(numero, "Ana Ruiz", "AHORRO", "1500.50", "ACTIVA");
        assertEquals(201, creada.statusCode());
        assertEquals(numero, creada.jsonPath().getString("numeroCuenta"));
        assertTrue(creada.jsonPath().getLong("id") > 0);
        assertEquals(0, dinero(creada, "saldo").compareTo(new BigDecimal("1500.50")));

        Response duplicada = crearCuenta(numero, "Otra persona", "CORRIENTE", "10.00", "ACTIVA");
        assertEquals(409, duplicada.statusCode());
        assertTrue(duplicada.jsonPath().getString("mensaje").contains(numero));

        Response negativa = crearCuenta(numero(), "Saldo malo", "AHORRO", "-1.00", "ACTIVA");
        assertEquals(400, negativa.statusCode());
        assertTrue(negativa.jsonPath().getString("mensaje").toLowerCase().contains("negativo"));

        Response corta = crearCuenta("123", "Corta", "AHORRO", "10.00", "ACTIVA");
        assertEquals(400, corta.statusCode());

        Response listado = given()
                .queryParam("tamano", 100)
                .queryParam("ordenarPor", "numeroCuenta")
                .when().get("/api/cuentas")
                .then().statusCode(200)
                .extract().response();
        assertTrue(listado.jsonPath().getList("contenido.numeroCuenta").contains(numero));
        assertTrue(listado.jsonPath().getInt("tamano") <= 100);
    }

    @Test
    void ordenaPorSaldoYPagina() {
        String bajo = numero();
        String alto = numero();
        crearCuenta(bajo, "Saldo bajo", "AHORRO", "5.00", "ACTIVA");
        crearCuenta(alto, "Saldo alto", "CORRIENTE", "50.00", "ACTIVA");

        Response orden = given()
                .queryParam("pagina", 0)
                .queryParam("tamano", 100)
                .queryParam("ordenarPor", "saldo")
                .queryParam("direccion", "asc")
                .when().get("/api/cuentas")
                .then().statusCode(200)
                .extract().response();
        List<String> numeros = orden.jsonPath().getList("contenido.numeroCuenta");
        assertTrue(numeros.indexOf(bajo) < numeros.indexOf(alto));

        Response pagina = given()
                .queryParam("pagina", 0)
                .queryParam("tamano", 1)
                .when().get("/api/cuentas")
                .then().statusCode(200)
                .extract().response();
        assertEquals(1, pagina.jsonPath().getList("contenido").size());
        assertTrue(pagina.jsonPath().getLong("totalElementos") > 1);
        assertTrue(pagina.jsonPath().getInt("totalPaginas") >= 2);
    }

    @Test
    void transfiereDeFormaAtomicaYDejaHuellaDelRechazo() {
        long origen = idCuenta(numero(), "Origen", "AHORRO", "1000.00", "ACTIVA");
        long destino = idCuenta(numero(), "Destino", "CORRIENTE", "100.00", "ACTIVA");

        Response ok = transferir(origen, destino, "400.00");
        assertEquals(201, ok.statusCode());
        assertEquals("COMPLETADA", ok.jsonPath().getString("estado"));
        assertFalse(ok.jsonPath().getBoolean("sospechosa"));
        assertEquals(0, dinero(ok, "cuentaOrigen.saldoActual").compareTo(new BigDecimal("600.00")));
        assertEquals(0, dinero(ok, "cuentaDestino.saldoActual").compareTo(new BigDecimal("500.00")));

        Response sinSaldo = transferir(origen, destino, "600.01");
        assertEquals(422, sinSaldo.statusCode());
        assertTrue(sinSaldo.jsonPath().getString("mensaje").contains("600.00"));

        Response resumen = given()
                .when().get("/api/cuentas/{id}/resumen", origen)
                .then().statusCode(200)
                .extract().response();
        assertEquals(0, dinero(resumen, "saldoActual").compareTo(new BigDecimal("600.00")));
        assertEquals(0, dinero(resumen, "totalTransferidoHoy").compareTo(new BigDecimal("400.00")));
        assertEquals(1, resumen.jsonPath().getLong("cantidadTransferenciasHoy"));

        Response rechazadas = given()
                .queryParam("cuentaOrigenId", origen)
                .queryParam("estado", "RECHAZADA")
                .queryParam("tamano", 20)
                .when().get("/api/transferencias")
                .then().statusCode(200)
                .extract().response();
        assertTrue(rechazadas.jsonPath().getList("contenido.estado").contains("RECHAZADA"));
        assertFalse(rechazadas.jsonPath().getList("contenido.estado").contains("COMPLETADA"));
    }

    @Test
    void marcaEnRevisionCuandoElMontoSuperaCincoMilYRespetaElLimiteDiario() {
        long origen = idCuenta(numero(), "Grande", "CORRIENTE", "20000.00", "ACTIVA");
        long destino = idCuenta(numero(), "Recibe", "AHORRO", "0.00", "ACTIVA");

        Response exacta = transferir(origen, destino, "5000.00");
        assertEquals(201, exacta.statusCode());
        assertEquals("COMPLETADA", exacta.jsonPath().getString("estado"));

        Response sospechosa = transferir(origen, destino, "5000.01");
        assertEquals(422, sospechosa.statusCode());
        assertTrue(sospechosa.jsonPath().getString("mensaje").contains("$5000.00"));

        // 5000 caben en el día. Un monto de 5000.01 no, porque el cupo restante es 5000.00.
        // Se usa otra cuenta para comprobar que 5000.01 sí se marca en revisión.
        long origenRevision = idCuenta(numero(), "Revision", "AHORRO", "8000.00", "ACTIVA");
        long destinoRevision = idCuenta(numero(), "Revision destino", "AHORRO", "0.00", "ACTIVA");
        Response enRevision = transferir(origenRevision, destinoRevision, "5000.01");
        assertEquals(201, enRevision.statusCode());
        assertEquals("EN_REVISION", enRevision.jsonPath().getString("estado"));
        assertTrue(enRevision.jsonPath().getBoolean("sospechosa"));
        assertTrue(enRevision.jsonPath().getString("motivo").contains("5,000"));
        assertEquals(0, dinero(enRevision, "cuentaOrigen.saldoActual").compareTo(new BigDecimal("2999.99")));

        Response resumen = given()
                .when().get("/api/cuentas/{id}/resumen", origenRevision)
                .then().statusCode(200)
                .extract().response();
        assertEquals(1, resumen.jsonPath().getLong("cantidadMarcadasSospechosas"));
        assertEquals(0, dinero(resumen, "totalTransferidoHoy").compareTo(new BigDecimal("5000.01")));
    }

    @Test
    void laSextaTransferenciaDeLaHoraQuedaEnRevision() {
        long origen = idCuenta(numero(), "Frecuente", "AHORRO", "100.00", "ACTIVA");
        long destino = idCuenta(numero(), "Frecuente destino", "CORRIENTE", "0.00", "ACTIVA");

        for (int i = 0; i < 5; i++) {
            Response hecha = transferir(origen, destino, "1.00");
            assertEquals(201, hecha.statusCode());
            assertEquals("COMPLETADA", hecha.jsonPath().getString("estado"));
        }
        Response sexta = transferir(origen, destino, "1.00");
        assertEquals(201, sexta.statusCode());
        assertEquals("EN_REVISION", sexta.jsonPath().getString("estado"));
        assertTrue(sexta.jsonPath().getString("motivo").contains("última hora"));
    }

    @Test
    void rechazaInactivaInexistenteYLaMismaCuenta() {
        long activa = idCuenta(numero(), "Activa", "AHORRO", "300.00", "ACTIVA");
        long inactiva = idCuenta(numero(), "Inactiva", "AHORRO", "300.00", "INACTIVA");

        assertEquals(422, transferir(inactiva, activa, "10.00").statusCode());
        assertEquals(422, transferir(activa, inactiva, "10.00").statusCode());
        assertEquals(404, transferir(activa, 9_999_999_999L, "10.00").statusCode());
        assertEquals(422, transferir(activa, activa, "10.00").statusCode());
        assertEquals(400, transferir(activa, inactiva, "0").statusCode());

        Response resumen = given().when().get("/api/cuentas/{id}/resumen", activa);
        assertEquals(0, dinero(resumen, "saldoActual").compareTo(new BigDecimal("300.00")));
    }

    @Test
    void filtraElHistorialPorFechaYLoEntregaDeLaMasRecienteALaMasAntigua() {
        long origen = idCuenta(numero(), "Historial", "CORRIENTE", "500.00", "ACTIVA");
        long destino = idCuenta(numero(), "Historial destino", "AHORRO", "0.00", "ACTIVA");
        transferir(origen, destino, "10.00");
        transferir(origen, destino, "20.00");

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        Response deHoy = given()
                .queryParam("cuentaOrigenId", origen)
                .queryParam("fechaDesde", hoy.toString())
                .queryParam("fechaHasta", hoy.toString())
                .queryParam("tamano", 20)
                .when().get("/api/transferencias")
                .then().statusCode(200)
                .extract().response();
        List<String> fechas = deHoy.jsonPath().getList("contenido.fecha");
        assertTrue(fechas.size() >= 2);
        assertTrue(fechas.get(0).compareTo(fechas.get(1)) >= 0);

        Response deAyer = given()
                .queryParam("cuentaOrigenId", origen)
                .queryParam("fechaHasta", hoy.minusDays(1).toString())
                .when().get("/api/transferencias")
                .then().statusCode(200)
                .extract().response();
        assertEquals(0, deAyer.jsonPath().getList("contenido").size());
    }

    @Test
    void elResumenDeUnaCuentaInexistenteEs404YSwaggerEstaDisponible() {
        given().when().get("/api/cuentas/999999/resumen").then().statusCode(404);
        given().when().get("/q/openapi").then().statusCode(200);
    }

    private static Response crearCuenta(String numero, String titular, String tipo, String saldo, String estado) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"numeroCuenta":"%s","titular":"%s","tipo":"%s","saldoInicial":%s,"estado":"%s"}
                        """.formatted(numero, titular, tipo, saldo, estado))
                .when().post("/api/cuentas")
                .then().extract().response();
    }

    private static long idCuenta(String numero, String titular, String tipo, String saldo, String estado) {
        Response respuesta = crearCuenta(numero, titular, tipo, saldo, estado);
        assertEquals(201, respuesta.statusCode(), respuesta.asString());
        return respuesta.jsonPath().getLong("id");
    }

    private static Response transferir(long origen, long destino, String monto) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"cuentaOrigenId":%d,"cuentaDestinoId":%d,"monto":%s}
                        """.formatted(origen, destino, monto))
                .when().post("/api/transferencias")
                .then().extract().response();
    }

    private static BigDecimal dinero(Response respuesta, String ruta) {
        return new BigDecimal(respuesta.jsonPath().get(ruta).toString());
    }

    private static String numero() {
        return Long.toString(SECUENCIA.getAndIncrement());
    }
}
