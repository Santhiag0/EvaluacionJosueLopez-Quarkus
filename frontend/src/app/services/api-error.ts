import { HttpErrorResponse } from '@angular/common/http';

export function mensajeError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const cuerpo = error.error as { mensaje?: string } | string | null;
    if (cuerpo && typeof cuerpo === 'object' && typeof cuerpo.mensaje === 'string' && cuerpo.mensaje.trim()) {
      return cuerpo.mensaje;
    }
    if (typeof cuerpo === 'string' && cuerpo.trim()) {
      return cuerpo;
    }
    if (error.status === 0) {
      return 'No hay conexión con el servidor. Comprueba que el entorno esté en ejecución.';
    }
    return `La solicitud falló con el código ${error.status}.`;
  }
  return 'No fue posible completar la operación.';
}
