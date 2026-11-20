import { NgIf } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import {
  Cuenta,
  ResumenCuenta,
  dinero,
  etiquetaEstadoCuenta,
  etiquetaTipo,
  mensajeError,
} from '../models';
import { CuentaService } from './cuenta.service';

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    MatTableModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.scss',
})
export class CuentasComponent implements OnInit, AfterViewInit {
  private readonly cuentas = inject(CuentaService);
  private readonly formularios = inject(FormBuilder);

  readonly columnas = ['numeroCuenta', 'titular', 'tipo', 'saldo', 'estado'];
  readonly datos = new MatTableDataSource<Cuenta>([]);
  readonly dinero = dinero;
  readonly etiquetaTipo = etiquetaTipo;
  readonly etiquetaEstadoCuenta = etiquetaEstadoCuenta;

  cargando = false;
  errorListado: string | null = null;
  mensajeAlta: string | null = null;
  errorAlta: string | null = null;
  enviando = false;
  seleccionada: Cuenta | null = null;
  resumen: ResumenCuenta | null = null;
  errorResumen: string | null = null;

  readonly alta = this.formularios.nonNullable.group({
    numeroCuenta: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    titular: ['', [Validators.required, Validators.maxLength(150)]],
    tipo: this.formularios.nonNullable.control<'AHORRO' | 'CORRIENTE'>('AHORRO', Validators.required),
    saldoInicial: [0, [Validators.required, Validators.min(0)]],
    estado: this.formularios.nonNullable.control<'ACTIVA' | 'INACTIVA'>('ACTIVA', Validators.required),
  });

  @ViewChild(MatSort) sort!: MatSort;

  ngOnInit(): void {
    this.datos.sortingDataAccessor = (cuenta, columna) => {
      if (columna === 'saldo') {
        return cuenta.saldo;
      }
      const valor = cuenta[columna as keyof Cuenta];
      return typeof valor === 'string' ? valor.toLowerCase() : Number(valor);
    };
    this.cargar();
  }

  ngAfterViewInit(): void {
    this.datos.sort = this.sort;
  }

  cargar(): void {
    this.cargando = true;
    this.errorListado = null;
    this.cuentas.listar(0, 100, 'id', 'asc').subscribe({
      next: (pagina) => {
        this.datos.data = pagina.content;
        this.cargando = false;
        if (this.sort) {
          this.datos.sort = this.sort;
        }
      },
      error: (error) => {
        this.cargando = false;
        this.errorListado = mensajeError(error);
      },
    });
  }

  registrar(): void {
    this.mensajeAlta = null;
    this.errorAlta = null;
    if (this.alta.invalid) {
      this.alta.markAllAsTouched();
      return;
    }
    this.enviando = true;
    const valor = this.alta.getRawValue();
    this.cuentas.crear({
      numeroCuenta: valor.numeroCuenta,
      titular: valor.titular.trim(),
      tipo: valor.tipo,
      saldoInicial: Number(valor.saldoInicial),
      estado: valor.estado,
    }).subscribe({
      next: (cuenta) => {
        this.enviando = false;
        this.mensajeAlta = `Cuenta ${cuenta.numeroCuenta} registrada con id ${cuenta.id}.`;
        this.alta.reset({ numeroCuenta: '', titular: '', tipo: 'AHORRO', saldoInicial: 0, estado: 'ACTIVA' });
        this.cargar();
      },
      error: (error) => {
        this.enviando = false;
        this.errorAlta = mensajeError(error);
      },
    });
  }

  elegir(cuenta: Cuenta): void {
    this.seleccionada = cuenta;
    this.resumen = null;
    this.errorResumen = null;
    this.cuentas.resumen(cuenta.id).subscribe({
      next: (resumen) => {
        this.resumen = resumen;
      },
      error: (error) => {
        this.errorResumen = mensajeError(error);
      },
    });
  }
}
