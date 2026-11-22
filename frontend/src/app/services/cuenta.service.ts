import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { Cuenta, CuentaRequest, Pagina, ResumenCuenta } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class CuentaService {
  private readonly http = inject(HttpClient);
  private readonly refresco = new Subject<void>();

  readonly cambios$ = this.refresco.asObservable();

  listar(pagina = 0, tamano = 10, ordenarPor = 'numeroCuenta', direccion: 'asc' | 'desc' = 'asc'): Observable<Pagina<Cuenta>> {
    const params = new HttpParams()
      .set('pagina', pagina)
      .set('tamano', tamano)
      .set('ordenarPor', ordenarPor)
      .set('direccion', direccion);
    return this.http.get<Pagina<Cuenta>>('/api/cuentas', { params });
  }

  crear(solicitud: CuentaRequest): Observable<Cuenta> {
    return this.http.post<Cuenta>('/api/cuentas', solicitud);
  }

  resumen(id: number): Observable<ResumenCuenta> {
    return this.http.get<ResumenCuenta>(`/api/cuentas/${id}/resumen`);
  }

  notificarCambio(): void {
    this.refresco.next();
  }
}
