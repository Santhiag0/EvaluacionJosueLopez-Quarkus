import { Component, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { Cuenta, Transferencia } from '../models/api.models';
import { mensajeError } from '../services/api-error';
import { CuentaService } from '../services/cuenta.service';
import { dinero } from '../services/formato';
import { TransferenciaService } from '../services/transferencia.service';

function cuentasDistintas(control: AbstractControl): ValidationErrors | null {
  const origen = control.get('cuentaOrigenId')?.value as number | null;
  const destino = control.get('cuentaDestinoId')?.value as number | null;
  if (origen == null || destino == null) {
    return null;
  }
  return origen === destino ? { mismaCuenta: true } : null;
}

@Component({
  selector: 'app-transferencia-form',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './transferencia-form.component.html'
})
export class TransferenciaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cuentasApi = inject(CuentaService);
  private readonly transferenciasApi = inject(TransferenciaService);

  readonly dinero = dinero;
  cuentas: Cuenta[] = [];
  enviando = false;
  errorCarga = '';
  resultado: Transferencia | null = null;
  error = '';

  formulario = this.fb.group({
    cuentaOrigenId: [null as number | null, Validators.required],
    cuentaDestinoId: [null as number | null, Validators.required],
    monto: [null as number | null, [Validators.required, Validators.min(0.01)]]
  }, { validators: cuentasDistintas });

  ngOnInit(): void {
    this.cargarCuentas();
  }

  cargarCuentas(): void {
    this.cuentasApi.listar(0, 100, 'numeroCuenta', 'asc').subscribe({
      next: (pagina) => {
        this.cuentas = pagina.contenido;
      },
      error: (error: unknown) => {
        this.errorCarga = mensajeError(error);
      }
    });
  }

  etiqueta(cuenta: Cuenta): string {
    const estado = cuenta.estado === 'ACTIVA' ? 'activa' : 'inactiva';
    return `${cuenta.numeroCuenta} · ${cuenta.titular} · ${dinero(cuenta.saldo)} · ${estado}`;
  }

  enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valor = this.formulario.getRawValue();
    if (valor.cuentaOrigenId == null || valor.cuentaDestinoId == null || valor.monto == null) {
      return;
    }
    this.enviando = true;
    this.error = '';
    this.resultado = null;
    this.transferenciasApi.ejecutar({
      cuentaOrigenId: valor.cuentaOrigenId,
      cuentaDestinoId: valor.cuentaDestinoId,
      monto: Number(valor.monto)
    }).subscribe({
      next: (transferencia) => {
        this.enviando = false;
        this.resultado = transferencia;
        this.formulario.reset();
        this.cuentasApi.notificarCambio();
        this.cargarCuentas();
      },
      error: (error: unknown) => {
        this.enviando = false;
        this.error = mensajeError(error);
      }
    });
  }

  errorCampo(nombre: 'cuentaOrigenId' | 'cuentaDestinoId' | 'monto'): string | null {
    const control = this.formulario.controls[nombre];
    if (!control.touched || !control.errors) {
      return null;
    }
    if (control.errors['required']) {
      return 'Campo obligatorio';
    }
    if (control.errors['min']) {
      return 'El monto debe ser numérico y mayor a cero';
    }
    return 'Valor inválido';
  }
}
