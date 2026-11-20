import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Pagina, Transferencia } from '../models';

export interface TransferenciaAlta {
  cuentaOrigenId: number;
  cuentaDestinoId: number;
  monto: number;
}

export interface FiltroHistorial {
  cuentaOrigenId?: number | null;
  cuentaDestinoId?: number | null;
  estado?: string | null;
  fechaDesde?: string | null;
  fechaHasta?: string | null;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class TransferenciaService {
  private readonly http = inject(HttpClient);

  crear(solicitud: TransferenciaAlta): Observable<Transferencia> {
    return this.http.post<Transferencia>('/api/transferencias', solicitud);
  }

  listar(filtro: FiltroHistorial): Observable<Pagina<Transferencia>> {
    let params = new HttpParams()
      .set('page', filtro.page ?? 0)
      .set('size', filtro.size ?? 10);
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
