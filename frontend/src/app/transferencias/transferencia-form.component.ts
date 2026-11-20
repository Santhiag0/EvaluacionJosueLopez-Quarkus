import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Cuenta, dinero, etiquetaEstadoCuenta, mensajeError } from '../models';
import { CuentaService } from '../cuentas/cuenta.service';
import { TransferenciaService } from './transferencia.service';

function cuentasDistintas(control: AbstractControl): ValidationErrors | null {
  const origen = control.get('cuentaOrigenId')?.value;
  const destino = control.get('cuentaDestinoId')?.value;
  if (origen != null && destino != null && origen === destino) {
    return { cuentasIguales: true };
  }
  return null;
}

@Component({
  selector: 'app-transferencia-form',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './transferencia-form.component.html',
  styleUrl: './transferencia-form.component.scss',
})
export class TransferenciaFormComponent implements OnInit {
  private readonly cuentas = inject(CuentaService);
  private readonly transferencias = inject(TransferenciaService);
  private readonly formularios = inject(FormBuilder);

  readonly dinero = dinero;
  readonly etiquetaEstadoCuenta = etiquetaEstadoCuenta;

  cuentasDisponibles: Cuenta[] = [];
  errorCarga: string | null = null;
  exito: string | null = null;
  error: string | null = null;
  enviando = false;

  readonly formulario = this.formularios.group(
    {
      cuentaOrigenId: [null as number | null, Validators.required],
      cuentaDestinoId: [null as number | null, Validators.required],
      monto: [null as number | null, [Validators.required, Validators.min(0.01)]],
    },
    { validators: cuentasDistintas },
  );

  ngOnInit(): void {
    this.cargarCuentas();
  }

  cargarCuentas(): void {
    this.cuentas.listar(0, 100, 'numeroCuenta', 'asc').subscribe({
      next: (pagina) => {
        this.cuentasDisponibles = pagina.content;
        this.errorCarga = null;
      },
      error: (error) => {
        this.errorCarga = mensajeError(error);
      },
    });
  }

  enviar(): void {
    this.exito = null;
    this.error = null;
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valor = this.formulario.getRawValue();
    this.enviando = true;
    this.transferencias.crear({
      cuentaOrigenId: Number(valor.cuentaOrigenId),
      cuentaDestinoId: Number(valor.cuentaDestinoId),
      monto: Number(valor.monto),
    }).subscribe({
      next: (transferencia) => {
        this.enviando = false;
        this.exito = transferencia.sospechosa
          ? `Transferencia ${transferencia.id} registrada y marcada para revisión. ${transferencia.motivoSospecha ?? ''}`
          : `Transferencia ${transferencia.id} completada por ${dinero(transferencia.monto)}.`;
        this.formulario.reset();
        this.cargarCuentas();
      },
      error: (error) => {
        this.enviando = false;
        this.error = mensajeError(error);
      },
    });
  }
}
