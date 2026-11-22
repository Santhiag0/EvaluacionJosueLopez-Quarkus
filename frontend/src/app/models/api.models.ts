export type TipoCuenta = 'AHORRO' | 'CORRIENTE';
export type EstadoCuenta = 'ACTIVA' | 'INACTIVA';
export type EstadoTransferencia = 'COMPLETADA' | 'RECHAZADA' | 'EN_REVISION';

export interface Cuenta {
  id: number;
  numeroCuenta: string;
  titular: string;
  tipo: TipoCuenta;
  saldo: number;
  estado: EstadoCuenta;
  fechaCreacion: string;
}

export interface CuentaRequest {
  numeroCuenta: string;
  titular: string;
  tipo: TipoCuenta;
  saldoInicial: number;
  estado: EstadoCuenta;
}

export interface ResumenCuenta {
  cuentaId: number;
  numeroCuenta: string;
  titular: string;
  saldoActual: number;
  totalTransferidoHoy: number;
  cantidadTransferenciasHoy: number;
  cantidadMarcadasSospechosas: number;
}

export interface CuentaOperacion {
  id: number;
  numeroCuenta: string;
  titular: string;
  saldoActual: number;
}

export interface Transferencia {
  id: number;
  cuentaOrigen: CuentaOperacion;
  cuentaDestino: CuentaOperacion;
  monto: number;
  fecha: string;
  estado: EstadoTransferencia;
  sospechosa: boolean;
  motivo: string | null;
}

export interface TransferenciaRequest {
  cuentaOrigenId: number;
  cuentaDestinoId: number;
  monto: number;
}

export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface ErrorApi {
  status: number;
  error: string;
  mensaje: string;
}
