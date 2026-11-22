-- Referencia del esquema. Al arrancar con Docker, Hibernate lo crea solo.
-- Este script sirve para una instalación manual sobre MySQL 8.

CREATE DATABASE IF NOT EXISTS transferencias
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE transferencias;

CREATE TABLE IF NOT EXISTS cuentas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  numero_cuenta VARCHAR(10) NOT NULL,
  titular VARCHAR(150) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  saldo DECIMAL(19, 2) NOT NULL,
  estado VARCHAR(20) NOT NULL,
  fecha_creacion DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cuentas_numero (numero_cuenta)
);

CREATE TABLE IF NOT EXISTS transferencias (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cuenta_origen_id BIGINT NOT NULL,
  cuenta_destino_id BIGINT NOT NULL,
  monto DECIMAL(19, 2) NOT NULL,
  fecha DATETIME(6) NOT NULL,
  estado VARCHAR(20) NOT NULL,
  sospechosa BIT(1) NOT NULL,
  motivo VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_transferencia_origen_fecha (cuenta_origen_id, fecha),
  KEY idx_transferencia_destino_fecha (cuenta_destino_id, fecha),
  KEY idx_transferencia_fecha (fecha),
  CONSTRAINT fk_transferencia_origen FOREIGN KEY (cuenta_origen_id) REFERENCES cuentas (id),
  CONSTRAINT fk_transferencia_destino FOREIGN KEY (cuenta_destino_id) REFERENCES cuentas (id)
);
