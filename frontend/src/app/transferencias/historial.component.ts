import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Cuenta, Transferencia, dinero, etiquetaEstadoTransferencia, mensajeError } from '../models';
import { CuentaService } from '../cuentas/cuenta.service';
import { TransferenciaService } from './transferencia.service';

@Component({
  selector: 'app-historial',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './historial.component.html',
  styleUrl: './historial.component.scss',
})
export class HistorialComponent implements OnInit {
  private readonly transferencias = inject(TransferenciaService);
  private readonly cuentas = inject(CuentaService);
  private readonly formularios = inject(FormBuilder);

  readonly dinero = dinero;
  readonly etiquetaEstado = etiquetaEstadoTransferencia;

  cuentasDisponibles: Cuenta[] = [];
  resultados: Transferencia[] = [];
  pagina = 0;
  totalPaginas = 0;
  total = 0;
  cargando = false;
  error: string | null = null;

  readonly filtros = this.formularios.group({
    cuentaOrigenId: [null as number | null],
    cuentaDestinoId: [null as number | null],
    estado: [''],
    fechaDesde: [''],
    fechaHasta: [''],
  });

  ngOnInit(): void {
    this.cuentas.listar(0, 100, 'numeroCuenta', 'asc').subscribe({
      next: (pagina) => {
        this.cuentasDisponibles = pagina.content;
      },
    });
    this.consultar(0);
  }

  consultar(pagina = 0): void {
    this.cargando = true;
    this.error = null;
    this.pagina = pagina;
    const valor = this.filtros.getRawValue();
    this.transferencias.listar({
      cuentaOrigenId: valor.cuentaOrigenId,
      cuentaDestinoId: valor.cuentaDestinoId,
      estado: valor.estado || null,
      fechaDesde: valor.fechaDesde || null,
      fechaHasta: valor.fechaHasta || null,
      page: pagina,
      size: 8,
    }).subscribe({
      next: (respuesta) => {
        this.resultados = respuesta.content;
        this.totalPaginas = respuesta.totalPages;
        this.total = respuesta.totalElements;
        this.cargando = false;
      },
      error: (error) => {
        this.cargando = false;
        this.error = mensajeError(error);
      },
    });
  }
}
