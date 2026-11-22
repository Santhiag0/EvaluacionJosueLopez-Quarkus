import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

import { Cuenta, EstadoTransferencia, Transferencia } from '../models/api.models';
import { mensajeError } from '../services/api-error';
import { CuentaService } from '../services/cuenta.service';
import { dinero, fechaHora } from '../services/formato';
import { TransferenciaService } from '../services/transferencia.service';

@Component({
  selector: 'app-historial',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule
  ],
  templateUrl: './historial.component.html'
})
export class HistorialComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly transferenciasApi = inject(TransferenciaService);
  private readonly cuentasApi = inject(CuentaService);

  readonly columnas = ['fecha', 'origen', 'destino', 'monto', 'estado', 'motivo'];
  readonly dinero = dinero;
  readonly fechaHora = fechaHora;

  cuentas: Cuenta[] = [];
  filas: Transferencia[] = [];
  total = 0;
  pagina = 0;
  tamano = 10;
  cargando = false;
  error = '';

  filtros = this.fb.group({
    cuentaOrigenId: [null as number | null],
    cuentaDestinoId: [null as number | null],
    estado: ['' as EstadoTransferencia | ''],
    fechaDesde: [''],
    fechaHasta: ['']
  });

  ngOnInit(): void {
    this.cuentasApi.listar(0, 100, 'numeroCuenta', 'asc').subscribe({
      next: (pagina) => {
        this.cuentas = pagina.contenido;
      }
    });
    this.consultar();
  }

  consultar(): void {
    this.pagina = 0;
    this.cargar();
  }

  cambiarPagina(evento: PageEvent): void {
    this.pagina = evento.pageIndex;
    this.tamano = evento.pageSize;
    this.cargar();
  }

  etiquetaEstado(estado: EstadoTransferencia): string {
    switch (estado) {
      case 'COMPLETADA':
        return 'Completada';
      case 'RECHAZADA':
        return 'Rechazada';
      case 'EN_REVISION':
        return 'En revisión';
    }
  }

  private cargar(): void {
    this.cargando = true;
    this.error = '';
    const valor = this.filtros.getRawValue();
    this.transferenciasApi.listar({
      pagina: this.pagina,
      tamano: this.tamano,
      cuentaOrigenId: valor.cuentaOrigenId,
      cuentaDestinoId: valor.cuentaDestinoId,
      estado: valor.estado ?? '',
      fechaDesde: valor.fechaDesde ?? '',
      fechaHasta: valor.fechaHasta ?? ''
    }).subscribe({
      next: (pagina) => {
        this.filas = pagina.contenido;
        this.total = pagina.totalElementos;
        this.cargando = false;
      },
      error: (error: unknown) => {
        this.error = mensajeError(error);
        this.cargando = false;
      }
    });
  }
}
