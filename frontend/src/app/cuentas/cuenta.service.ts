import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cuenta, CuentaAlta, Pagina, ResumenCuenta } from '../models';

@Injectable({ providedIn: 'root' })
export class CuentaService {
  private readonly http = inject(HttpClient);

  listar(page = 0, size = 100, sort = 'saldo', direction: 'asc' | 'desc' = 'desc'): Observable<Pagina<Cuenta>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort)
      .set('direction', direction);
    return this.http.get<Pagina<Cuenta>>('/api/cuentas', { params });
  }

  crear(cuenta: CuentaAlta): Observable<Cuenta> {
    return this.http.post<Cuenta>('/api/cuentas', cuenta);
  }

  resumen(id: number): Observable<ResumenCuenta> {
    return this.http.get<ResumenCuenta>(`/api/cuentas/${id}/resumen`);
  }
}
