# EvaluacionJosueLopez

Sistema de gestión de transferencias interbancarias. API REST con Java 21 y Quarkus, interfaz en Angular y MySQL.

## Arranque con un comando

Hace falta Docker Desktop encendido. En la raíz del repositorio:

```bash
docker compose up --build -d
```

La primera vez descarga las imágenes y compila backend y frontend. Puede tardar varios minutos.

En Windows, este comando también abre el navegador cuando la aplicación responde:

```powershell
powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
```

| Qué | URL |
| --- | --- |
| Interfaz | http://localhost:8088 |
| Swagger | http://localhost:8088/q/swagger-ui |
| OpenAPI | http://localhost:8088/q/openapi |
| API directa | http://localhost:8080 |
| Salud del API | http://localhost:8080/q/health |

MySQL no publica el puerto 3306 del equipo, para no chocar con una instalación local. La base del contenedor se llama `transferencias`, usuario `app`, contraseña `app`. El usuario root es `root`. Para consultarla:

```bash
docker compose exec db mysql -uapp -papp transferencias
```

Al primer arranque el API crea las tablas y carga seis cuentas de demostración más una transferencia de $250. Para volver a empezar desde cero:

```bash
docker compose down -v
docker compose up --build -d
```

Para detener sin borrar los datos: `docker compose down`.

## Qué hace el sistema

- Registra cuentas con número único de 10 dígitos, titular, tipo (ahorro o corriente), saldo inicial mayor o igual a cero y estado.
- Transfiere solo si las dos cuentas existen, están activas, son distintas y la origen tiene saldo. Los dos saldos se actualizan en la misma transacción. Las filas se bloquean en orden de id para que dos transferencias cruzadas no se bloqueen entre sí.
- Marca la transferencia como `EN_REVISION` cuando el monto supera $5,000 o cuando esta operación es la sexta salida exitosa de la cuenta en los últimos 60 minutos. El dinero sí se mueve.
- Rechaza la operación si el acumulado del día calendario, en `America/Bogota`, pasaría de $10,000. La respuesta dice cuánto queda disponible. Un rechazo no mueve saldos, pero queda en el historial como `RECHAZADA`.
- $5,000 exactos no son sospechosos. $10,000 exactos en el día sí están permitidos.
- Lista cuentas y transferencias con paginación. El historial acepta filtros por origen, destino, estado y fechas, y siempre vuelve ordenado de la fecha más reciente a la más antigua.
- `GET /api/cuentas/{id}/resumen` calcula saldo actual, total transferido hoy, cantidad de transferencias del día y cantidad histórica de salidas sospechosas.

## API

| Método | Ruta | Uso |
| --- | --- | --- |
| POST | `/api/cuentas` | Crea una cuenta. 201, 400 o 409 |
| GET | `/api/cuentas` | Lista paginada. `pagina`, `tamano`, `ordenarPor`, `direccion` |
| GET | `/api/cuentas/{id}/resumen` | Resumen de la cuenta |
| POST | `/api/transferencias` | Ejecuta una transferencia. 201, 400, 404 o 422 |
| GET | `/api/transferencias` | Historial. `cuentaOrigenId`, `cuentaDestinoId`, `estado`, `fechaDesde`, `fechaHasta`, `pagina`, `tamano` |

Ejemplo de alta:

```json
{
  "numeroCuenta": "2000000001",
  "titular": "Josué López",
  "tipo": "AHORRO",
  "saldoInicial": 1000.00,
  "estado": "ACTIVA"
}
```

Ejemplo de transferencia:

```json
{
  "cuentaOrigenId": 1,
  "cuentaDestinoId": 2,
  "monto": 250.00
}
```

Los errores tienen esta forma:

```json
{
  "marcaTiempo": "2026-11-22T10:15:00-05:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "mensaje": "La transferencia excede el límite diario de $10,000.00. Monto restante disponible: $4000.00"
}
```

La interfaz está en Angular Material: directorio de cuentas con orden por saldo, alta de cuentas, formulario de transferencia con validación en el cliente y historial con filtros. Nginx publica la interfaz y reenvía `/api` y `/q` al backend, así que el navegador habla con un solo origen.

## Arquitectura

```
api            Recursos REST. Validan la entrada y delegan.
servicio       Reglas de negocio y transacción.
repositorio    Consultas Panache y JPQL.
dominio        Entidades y enumeraciones.
dto            Contratos de entrada y salida. Las entidades no salen del API.
mapeador       Conversión manual entre entidad y DTO.
excepcion      Un mapper central para 400, 404, 409, 422 y 500.
```

En producción los logs de consola salen en JSON. CORS permite el frontend de Docker (`http://localhost:8088`) y `ng serve` (`http://localhost:4200`).

## Pruebas

Con Docker, sin instalar Maven:

```bash
docker compose --profile test run --rm tests
```

Eso ejecuta las pruebas unitarias de detección sospechosa, límite diario y transferencias (JUnit 5 y Mockito) y las de integración `@QuarkusTest` contra H2 en memoria.

## Desarrollo sin Docker

Backend, con Maven y Java 21, y un MySQL local llamado `transferencias`:

```bash
cd backend
mvn quarkus:dev
```

Para cargar las cuentas de demostración: `-Dapp.seed.enabled=true`.

Frontend:

```bash
cd frontend
npm install
npm start
```

`ng serve` queda en http://localhost:4200 y el proxy envía `/api` a http://localhost:8080.

El esquema de referencia, por si se crean las tablas a mano, está en `db/schema.sql`. Con Docker no hace falta ejecutarlo: Hibernate actualiza el esquema al arrancar.
