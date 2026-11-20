export interface Cuenta {
  id: number;
  numeroCuenta: string;
  titular: string;
  tipo: 'AHORRO' | 'CORRIENTE';
  saldo: number;
  estado: 'ACTIVA' | 'INACTIVA';
  fechaCreacion: string;
}

export interface CuentaAlta {
  numeroCuenta: string;
  titular: string;
  tipo: 'AHORRO' | 'CORRIENTE';
  saldoInicial: number;
  estado: 'ACTIVA' | 'INACTIVA';
}

export interface ResumenCuenta {
  cuentaId: number;
  numeroCuenta: string;
  titular: string;
  saldoActual: number;
  totalTransferidoHoy: number;
  cantidadTransferenciasDelDia: number;
  cantidadMarcadasComoSospechosas: number;
}

export interface Transferencia {
  id: number;
  cuentaOrigenId: number;
  numeroCuentaOrigen: string;
  titularOrigen: string;
  cuentaDestinoId: number;
  numeroCuentaDestino: string;
  titularDestino: string;
  monto: number;
  estado: 'COMPLETADA' | 'RECHAZADA' | 'EN_REVISION';
  sospechosa: boolean;
  motivoSospecha: string | null;
  motivoRechazo: string | null;
  fecha: string;
}

export interface Pagina<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ErrorApi {
  codigo?: string;
  mensaje?: string;
}

export function dinero(valor: number | null | undefined): string {
  const monto = valor ?? 0;
  return '$' + monto.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export function etiquetaTipo(tipo: string): string {
  return tipo === 'CORRIENTE' ? 'Corriente' : 'Ahorro';
}

export function etiquetaEstadoCuenta(estado: string): string {
  return estado === 'INACTIVA' ? 'Inactiva' : 'Activa';
}

export function etiquetaEstadoTransferencia(estado: string): string {
  if (estado === 'EN_REVISION') {
    return 'En revisión';
  }
  if (estado === 'RECHAZADA') {
    return 'Rechazada';
  }
  return 'Completada';
}

export function mensajeError(error: unknown): string {
  const cuerpo = (error as { error?: ErrorApi })?.error;
  if (cuerpo?.mensaje) {
    return cuerpo.mensaje;
  }
  return 'No se pudo conectar con el servidor. Intente de nuevo en unos segundos.';
}
