package com.evaluacion.josuesantiagolopez.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TransferenciasApiTest {

    private static final AtomicInteger SECUENCIA = new AtomicInteger(1000);

    @Test
    void registraCuentaListaYRechazaDuplicadoYSaldoNegativo() {
        String numero = numeroCuenta();
        Number id = crearCuenta(numero, "Josue Santiago Lopez", "ahorro", "2500.50", "activa");

        given()
                .when().get("/api/cuentas/{id}", id)
                .then().statusCode(200)
                .body("numeroCuenta", equalTo(numero))
                .body("titular", equalTo("Josue Santiago Lopez"))
                .body("tipo", equalTo("AHORRO"))
                .body("estado", equalTo("ACTIVA"))
                .body("saldo", cercaDe(2500.50));

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoCuenta(numero, "Otra persona", "CORRIENTE", "10.00", "ACTIVA"))
                .when().post("/api/cuentas")
                .then().statusCode(409)
                .body("codigo", equalTo("CUENTA_DUPLICADA"));

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoCuenta(numeroCuenta(), "Saldo malo", "AHORRO", "-1.00", "ACTIVA"))
                .when().post("/api/cuentas")
                .then().statusCode(400);

        given()
                .queryParam("page", 0)
                .queryParam("size", 1)
                .queryParam("sort", "saldo")
                .queryParam("direction", "desc")
                .when().get("/api/cuentas")
                .then().statusCode(200)
                .body("content", hasSize(1))
                .body("totalElements", greaterThanOrEqualTo(1))
                .body("page", equalTo(0));
    }

    @Test
    void transfiereDeFormaAtomicaYConservaSaldosSiFalla() {
        Number origen = crearCuenta(numeroCuenta(), "Origen ok", "CORRIENTE", "1000.00", "ACTIVA");
        Number destino = crearCuenta(numeroCuenta(), "Destino ok", "AHORRO", "100.00", "ACTIVA");
        Number inactiva = crearCuenta(numeroCuenta(), "Destino inactivo", "AHORRO", "100.00", "INACTIVA");

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origen, destino, "250.00"))
                .when().post("/api/transferencias")
                .then().statusCode(201)
                .body("estado", equalTo("COMPLETADA"))
                .body("sospechosa", equalTo(false))
                .body("id", notNullValue());

        saldo(origen, 750.00);
        saldo(destino, 350.00);

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origen, destino, "900.00"))
                .when().post("/api/transferencias")
                .then().statusCode(422)
                .body("codigo", equalTo("SALDO_INSUFICIENTE"))
                .body("mensaje", org.hamcrest.Matchers.containsString("750.00"));

        saldo(origen, 750.00);
        saldo(destino, 350.00);

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origen, inactiva, "10.00"))
                .when().post("/api/transferencias")
                .then().statusCode(422)
                .body("codigo", equalTo("CUENTA_DESTINO_INACTIVA"));

        saldo(origen, 750.00);

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origen, origen, "10.00"))
                .when().post("/api/transferencias")
                .then().statusCode(400)
                .body("codigo", equalTo("CUENTAS_IGUALES"));

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(99999999, destino, "10.00"))
                .when().post("/api/transferencias")
                .then().statusCode(404)
                .body("codigo", equalTo("NO_ENCONTRADO"));
    }

    @Test
    void marcaSospechosaPorMontoYPorFrecuencia() {
        Number origen = crearCuenta(numeroCuenta(), "Origen sospecha", "AHORRO", "20000.00", "ACTIVA");
        Number destino = crearCuenta(numeroCuenta(), "Destino sospecha", "CORRIENTE", "0.00", "ACTIVA");

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origen, destino, "5000.00"))
                .when().post("/api/transferencias")
                .then().statusCode(201)
                .body("estado", equalTo("COMPLETADA"));

        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origen, destino, "5000.01"))
                .when().post("/api/transferencias")
                .then().statusCode(422)
                .body("codigo", equalTo("LIMITE_DIARIO_EXCEDIDO"))
                .body("mensaje", org.hamcrest.Matchers.containsString("Monto restante disponible: $5000.00"));

        saldo(origen, 15000.00);

        Number origenMonto = crearCuenta(numeroCuenta(), "Origen monto", "AHORRO", "8000.00", "ACTIVA");
        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origenMonto, destino, "5000.01"))
                .when().post("/api/transferencias")
                .then().statusCode(201)
                .body("estado", equalTo("EN_REVISION"))
                .body("sospechosa", equalTo(true));

        Number origenFrecuencia = crearCuenta(numeroCuenta(), "Origen frecuencia", "CORRIENTE", "5000.00", "ACTIVA");
        for (int i = 0; i < 6; i++) {
            given()
                    .contentType(ContentType.JSON)
                    .body(cuerpoTransferencia(origenFrecuencia, destino, "10.00"))
                    .when().post("/api/transferencias")
                    .then().statusCode(201)
                    .body("estado", equalTo("COMPLETADA"));
        }
        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origenFrecuencia, destino, "10.00"))
                .when().post("/api/transferencias")
                .then().statusCode(201)
                .body("estado", equalTo("EN_REVISION"))
                .body("sospechosa", equalTo(true));
    }

    @Test
    void consultaHistorialYResumen() {
        Number origen = crearCuenta(numeroCuenta(), "Origen historial", "AHORRO", "3000.00", "ACTIVA");
        Number destino = crearCuenta(numeroCuenta(), "Destino historial", "AHORRO", "100.00", "ACTIVA");
        given()
                .contentType(ContentType.JSON)
                .body(cuerpoTransferencia(origen, destino, "125.50"))
                .when().post("/api/transferencias")
                .then().statusCode(201);

        String hoy = LocalDate.now(ZoneId.of("America/Guayaquil")).toString();
        String manana = LocalDate.now(ZoneId.of("America/Guayaquil")).plusDays(1).toString();

        given()
                .queryParam("cuentaOrigenId", origen)
                .queryParam("cuentaDestinoId", destino)
                .queryParam("estado", "completada")
                .queryParam("fechaDesde", hoy)
                .queryParam("fechaHasta", hoy)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/api/transferencias")
                .then().statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].estado", equalTo("COMPLETADA"))
                .body("content[0].monto", cercaDe(125.50));

        given()
                .queryParam("cuentaOrigenId", origen)
                .queryParam("fechaDesde", manana)
                .when().get("/api/transferencias")
                .then().statusCode(200)
                .body("totalElements", equalTo(0));

        given()
                .queryParam("cuentaOrigenId", origen)
                .queryParam("estado", "en revisión")
                .when().get("/api/transferencias")
                .then().statusCode(200)
                .body("totalElements", equalTo(0));

        given()
                .when().get("/api/cuentas/{id}/resumen", origen)
                .then().statusCode(200)
                .body("saldoActual", cercaDe(2874.50))
                .body("totalTransferidoHoy", cercaDe(125.50))
                .body("cantidadTransferenciasDelDia", equalTo(1))
                .body("cantidadMarcadasComoSospechosas", equalTo(0));

        given()
                .when().get("/api/cuentas/99999999/resumen")
                .then().statusCode(404);
    }

    @Test
    void publicaOpenApi() {
        String especificacion = given()
                .when().get("/q/openapi")
                .then().statusCode(200)
                .extract().asString();
        assertTrue(especificacion.contains("/api/cuentas"));
        assertTrue(especificacion.contains("/api/transferencias"));
        assertTrue(especificacion.contains("EN_REVISION") || especificacion.contains("en revisión")
                || especificacion.contains("Transferencia"));
    }

    private static void saldo(Number id, double esperado) {
        given()
                .when().get("/api/cuentas/{id}", id)
                .then().statusCode(200)
                .body("saldo", cercaDe(esperado));
    }

    private static Number crearCuenta(String numero, String titular, String tipo, String saldo, String estado) {
        Response respuesta = given()
                .contentType(ContentType.JSON)
                .body(cuerpoCuenta(numero, titular, tipo, saldo, estado))
                .when().post("/api/cuentas")
                .then().statusCode(201)
                .extract().response();
        return respuesta.path("id");
    }

    private static String cuerpoCuenta(String numero, String titular, String tipo, String saldo, String estado) {
        return """
                {"numeroCuenta":"%s","titular":"%s","tipo":"%s","saldoInicial":%s,"estado":"%s"}
                """.formatted(numero, titular, tipo, saldo, estado);
    }

    private static String cuerpoTransferencia(Number origen, Number destino, String monto) {
        return """
                {"cuentaOrigenId":%s,"cuentaDestinoId":%s,"monto":%s}
                """.formatted(origen, destino, monto);
    }

    private static Matcher<Number> cercaDe(double esperado) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Number item) {
                return Math.abs(item.doubleValue() - esperado) < 0.001;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("un monto cercano a ").appendValue(esperado);
            }
        };
    }

    private static String numeroCuenta() {
        int secuencia = SECUENCIA.incrementAndGet();
        long cola = Math.floorMod(System.nanoTime(), 1_000_000L);
        return String.format("%04d%06d", secuencia % 10000, cola);
    }
}
