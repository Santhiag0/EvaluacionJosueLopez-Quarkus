import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { Sort } from '@angular/material/sort';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';

import { Cuenta, EstadoCuenta, ResumenCuenta, TipoCuenta } from '../models/api.models';
import { mensajeError } from '../services/api-error';
import { CuentaService } from '../services/cuenta.service';
import { dinero } from '../services/formato';

@Component({
  selector: 'app-cuentas',
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './cuentas.component.html'
})
export class CuentasComponent implements OnInit {
  private readonly cuentasApi = inject(CuentaService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly columnas = ['numeroCuenta', 'titular', 'tipo', 'saldo', 'estado', 'acciones'];
  readonly dinero = dinero;

  filas: Cuenta[] = [];
  total = 0;
  pagina = 0;
  tamano = 5;
  ordenarPor = 'numeroCuenta';
  direccion: 'asc' | 'desc' = 'asc';
  cargando = false;
  guardando = false;
  errorCarga = '';
  mensaje = '';
  mensajeEsError = false;
  resumen: ResumenCuenta | null = null;
  resumenError = '';
  indicadores = { total: 0, activas: 0, saldo: 0, parcial: false };

  formulario = this.fb.nonNullable.group({
    numeroCuenta: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    titular: ['', [Validators.required, Validators.maxLength(150)]],
    tipo: ['AHORRO' as TipoCuenta, Validators.required],
    saldoInicial: [0, [Validators.required, Validators.min(0)]],
    estado: ['ACTIVA' as EstadoCuenta, Validators.required]
  });

  ngOnInit(): void {
    this.cargar();
    this.cargarIndicadores();
    this.cuentasApi.cambios$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.cargar();
        this.cargarIndicadores();
      });
  }

  cargar(): void {
    this.cargando = true;
    this.errorCarga = '';
    this.cuentasApi.listar(this.pagina, this.tamano, this.ordenarPor, this.direccion).subscribe({
      next: (pagina) => {
        this.filas = pagina.contenido;
        this.total = pagina.totalElementos;
        this.cargando = false;
      },
      error: (error: unknown) => {
        this.errorCarga = mensajeError(error);
        this.cargando = false;
      }
    });
  }

  cargarIndicadores(): void {
    this.cuentasApi.listar(0, 100, 'numeroCuenta', 'asc').subscribe({
      next: (pagina) => {
        const centavos = pagina.contenido.reduce((suma, cuenta) => suma + Math.round(cuenta.saldo * 100), 0);
        this.indicadores = {
          total: pagina.totalElementos,
          activas: pagina.contenido.filter((cuenta) => cuenta.estado === 'ACTIVA').length,
          saldo: centavos / 100,
          parcial: pagina.totalElementos > pagina.contenido.length
        };
      }
    });
  }

  cambiarOrden(evento: Sort): void {
    if (!evento.direction || (evento.active !== 'saldo' && evento.active !== 'titular' && evento.active !== 'numeroCuenta')) {
      this.ordenarPor = 'numeroCuenta';
      this.direccion = 'asc';
    } else {
      this.ordenarPor = evento.active;
      this.direccion = evento.direction;
    }
    this.pagina = 0;
    this.cargar();
  }

  cambiarPagina(evento: PageEvent): void {
    this.pagina = evento.pageIndex;
    this.tamano = evento.pageSize;
    this.cargar();
  }

  verResumen(cuenta: Cuenta): void {
    this.resumen = null;
    this.resumenError = '';
    this.cuentasApi.resumen(cuenta.id).subscribe({
      next: (resumen) => {
        this.resumen = resumen;
      },
      error: (error: unknown) => {
        this.resumenError = mensajeError(error);
      }
    });
  }

  crear(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.guardando = true;
    this.mensaje = '';
    const valor = this.formulario.getRawValue();
    this.cuentasApi.crear({
      numeroCuenta: valor.numeroCuenta,
      titular: valor.titular.trim(),
      tipo: valor.tipo,
      saldoInicial: Number(valor.saldoInicial),
      estado: valor.estado
    }).subscribe({
      next: (cuenta) => {
        this.guardando = false;
        this.mensajeEsError = false;
        this.mensaje = `Cuenta ${cuenta.numeroCuenta} registrada para ${cuenta.titular}.`;
        this.formulario.reset({ numeroCuenta: '', titular: '', tipo: 'AHORRO', saldoInicial: 0, estado: 'ACTIVA' });
        this.cuentasApi.notificarCambio();
      },
      error: (error: unknown) => {
        this.guardando = false;
        this.mensajeEsError = true;
        this.mensaje = mensajeError(error);
      }
    });
  }

  errorCampo(nombre: 'numeroCuenta' | 'titular' | 'saldoInicial'): string | null {
    const control = this.formulario.controls[nombre];
    if (!control.touched || !control.errors) {
      return null;
    }
    if (control.errors['required']) {
      return 'Campo obligatorio';
    }
    if (control.errors['pattern']) {
      return 'Debe tener exactamente 10 dígitos';
    }
    if (control.errors['min']) {
      return 'El saldo no puede ser negativo';
    }
    if (control.errors['maxlength']) {
      return 'Máximo 150 caracteres';
    }
    return 'Valor inválido';
  }
}
