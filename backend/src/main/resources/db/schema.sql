-- Esquema de referencia para MySQL 8.
-- La aplicación también crea y actualiza el esquema al arrancar
-- (quarkus.hibernate-orm.schema-management.strategy=update).

CREATE DATABASE IF NOT EXISTS transferencias
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE transferencias;

CREATE TABLE IF NOT EXISTS cuentas (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    numero_cuenta   VARCHAR(10)    NOT NULL,
    titular         VARCHAR(150)   NOT NULL,
    tipo            VARCHAR(20)    NOT NULL,
    saldo           DECIMAL(16, 2) NOT NULL,
    estado          VARCHAR(20)    NOT NULL,
    fecha_creacion  DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_cuentas_numero UNIQUE (numero_cuenta)
);

CREATE TABLE IF NOT EXISTS transferencias (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    cuenta_origen_id   BIGINT         NOT NULL,
    cuenta_destino_id  BIGINT         NOT NULL,
    monto              DECIMAL(16, 2) NOT NULL,
    estado             VARCHAR(20)    NOT NULL,
    sospechosa         BOOLEAN        NOT NULL,
    motivo_sospecha    VARCHAR(500)   NULL,
    motivo_rechazo     VARCHAR(500)   NULL,
    fecha              DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transf_origen FOREIGN KEY (cuenta_origen_id) REFERENCES cuentas (id),
    CONSTRAINT fk_transf_destino FOREIGN KEY (cuenta_destino_id) REFERENCES cuentas (id),
    KEY idx_transf_origen_fecha (cuenta_origen_id, fecha),
    KEY idx_transf_destino_fecha (cuenta_destino_id, fecha),
    KEY idx_transf_estado (estado)
);
