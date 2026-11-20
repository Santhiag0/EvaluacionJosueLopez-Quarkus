# EvaluacionJosueSantiagoLopez

Sistema de gestión de transferencias interbancarias.

Autor: **Josue Santiago Lopez**

Backend en Java 21 con Quarkus (RESTEasy Reactive, Hibernate ORM con Panache), frontend en Angular 18 con Angular Material y base de datos MySQL 8. La API está documentada con Swagger / OpenAPI.

## Qué hace

- Registra cuentas con número único de 10 dígitos, titular, tipo (ahorro o corriente), saldo inicial y estado (activa o inactiva).
- Ejecuta transferencias atómicas: se actualizan los dos saldos en una sola transacción, o no se actualiza ninguno.
- Marca la operación como **en revisión** si el monto supera $5,000.00 o si la cuenta origen ya hizo más de 5 transferencias exitosas en la última hora.
- Rechaza la transferencia si el acumulado del día calendario superaría $10,000.00 e informa el monto restante.
- Consulta el historial con filtros y el resumen de cada cuenta.

El día calendario y el rango de fechas usan la zona `America/Guayaquil`.

## Levantar todo con Docker

Requisito: Docker con Compose.

```bash
docker compose up --build
```

| Servicio | URL |
| --- | --- |
| Dashboard Angular | http://localhost:8091 |
| API | http://localhost:8083 |
| Swagger UI | http://localhost:8083/q/swagger-ui |
| OpenAPI | http://localhost:8083/q/openapi |
| MySQL | `localhost:3317`, base `transferencias`, usuario y clave `transferencias` |

El frontend publicado en el puerto 8091 reenvía `/api` y `/q` al backend, así que Swagger también queda en http://localhost:8091/q/swagger-ui.

Al arrancar con la base vacía se crean cinco cuentas de demostración, incluida la de Josue Santiago Lopez (`1000000005`).

Para detenerlo:

```bash
docker compose down
```

## Ejecutar sin Docker

1. Crear la base. El script de referencia está en `backend/src/main/resources/db/schema.sql`. Hibernate también crea el esquema al iniciar (`schema-management.strategy=update`).
2. Ajustar la URL si MySQL no está en el puerto 3317. Por defecto el backend usa:

```text
jdbc:mysql://localhost:3317/transferencias
usuario: transferencias
clave: transferencias
```

3. Backend, con JDK 21 y Maven:

```bash
cd backend
mvn quarkus:dev
```

4. Frontend, con Node 20 o superior:

```bash
cd frontend
npm install
npm start
```

El servidor de desarrollo queda en http://localhost:4200 y el proxy envía `/api` a http://localhost:8083.

## Pruebas

Desde `backend`, con JDK 21:

```bash
mvn test
```

- Pruebas unitarias con JUnit 5 y Mockito: detección de operaciones sospechosas, límite diario y transferencia (saldos, rechazo, atomicidad de las reglas).
- Pruebas de integración con `@QuarkusTest` y H2 en memoria: endpoints, códigos HTTP, filtros, resumen y OpenAPI.

## API

### Cuentas

| Método | Ruta | Resultado |
| --- | --- | --- |
| `POST` | `/api/cuentas` | 201 cuenta creada. 400 datos inválidos o saldo negativo. 409 número duplicado |
| `GET` | `/api/cuentas?page=0&size=20&sort=saldo&direction=desc` | Página de cuentas |
| `GET` | `/api/cuentas/{id}` | 200 o 404 |
| `PATCH` | `/api/cuentas/{id}` | Cambia el estado a `ACTIVA` o `INACTIVA` |
| `GET` | `/api/cuentas/{id}/resumen` | Saldo actual, total transferido hoy, cantidad del día y cantidad sospechosa del día |

Ejemplo:

```bash
curl -s -X POST http://localhost:8083/api/cuentas \
  -H 'Content-Type: application/json' \
  -d '{"numeroCuenta":"5550000001","titular":"Josue Santiago Lopez","tipo":"AHORRO","saldoInicial":1000.00,"estado":"ACTIVA"}'
```

`tipo` acepta `AHORRO` o `ahorro`. `estado` acepta `ACTIVA` o `activa`.

### Transferencias

| Método | Ruta | Resultado |
| --- | --- | --- |
| `POST` | `/api/transferencias` | 201 completada o en revisión. 400 monto o cuentas iguales. 404 cuenta inexistente. 422 inactiva, saldo insuficiente o límite diario |
| `GET` | `/api/transferencias` | Historial ordenado por fecha descendente |

Filtros opcionales de `GET /api/transferencias`: `cuentaOrigenId`, `cuentaDestinoId`, `estado` (`COMPLETADA`, `RECHAZADA`, `EN_REVISION` o `en revisión`), `fechaDesde`, `fechaHasta` (`yyyy-MM-dd`), `page` y `size`.

Ejemplo:

```bash
curl -s -X POST http://localhost:8083/api/transferencias \
  -H 'Content-Type: application/json' \
  -d '{"cuentaOrigenId":1,"cuentaDestinoId":2,"monto":150.50}'
```

El cuerpo de error siempre trae `codigo`, `mensaje` y `timestamp`. Si el límite diario no alcanza, el mensaje incluye el monto restante disponible.

## Reglas que quedan registradas

- Una transferencia rechazada no mueve saldo y se guarda con estado `RECHAZADA`, así el historial puede filtrarla.
- Una transferencia sospechosa sí mueve el dinero y queda en `EN_REVISION`.
- El conteo de “más de 5 en la última hora” usa solo transferencias completadas o en revisión ya registradas. La sexta del periodo sigue siendo normal; a partir de la séptima se marca.
- El límite diario de $10,000.00 es inclusive: transferir exactamente el restante está permitido.
- Las dos cuentas se bloquean en orden de id para que la transacción sea atómica y no se cruce con otra transferencia de las mismas cuentas.

## Estructura

```text
backend/src/main/java/com/evaluacion/josuesantiagolopez
  resource/      CuentaResource, TransferenciaResource
  service/       reglas de negocio
  repository/    acceso a datos con Panache
  domain/        entidades JPA
  dto/ mapper/   contratos de entrada y salida
  exception/     ExceptionMapper centralizado
frontend/src/app
  cuentas/                 cuentas.component, cuenta.service
  transferencias/          transferencia-form.component, transferencia.service, historial
```

La lógica de negocio está en los servicios. Los recursos solo delegan y los repositorios solo consultan o persisten.
