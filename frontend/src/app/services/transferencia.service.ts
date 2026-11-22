import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { EstadoTransferencia, Pagina, Transferencia, TransferenciaRequest } from '../models/api.models';

export interface FiltroHistorial {
  pagina: number;
  tamano: number;
  cuentaOrigenId?: number | null;
  cuentaDestinoId?: number | null;
  estado?: EstadoTransferencia | '';
  fechaDesde?: string;
  fechaHasta?: string;
}

@Injectable({ providedIn: 'root' })
export class TransferenciaService {
  private readonly http = inject(HttpClient);

  ejecutar(solicitud: TransferenciaRequest): Observable<Transferencia> {
    return this.http.post<Transferencia>('/api/transferencias', solicitud);
  }

  listar(filtro: FiltroHistorial): Observable<Pagina<Transferencia>> {
    let params = new HttpParams()
      .set('pagina', filtro.pagina)
      .set('tamano', filtro.tamano);
    if (filtro.cuentaOrigenId) {
      params = params.set('cuentaOrigenId', filtro.cuentaOrigenId);
    }
    if (filtro.cuentaDestinoId) {
      params = params.set('cuentaDestinoId', filtro.cuentaDestinoId);
    }
    if (filtro.estado) {
      params = params.set('estado', filtro.estado);
    }
    if (filtro.fechaDesde) {
      params = params.set('fechaDesde', filtro.fechaDesde);
    }
    if (filtro.fechaHasta) {
      params = params.set('fechaHasta', filtro.fechaHasta);
    }
    return this.http.get<Pagina<Transferencia>>('/api/transferencias', { params });
  }
}
