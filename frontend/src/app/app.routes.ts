import { Routes } from '@angular/router';
import { CuentasComponent } from './cuentas/cuentas.component';
import { HistorialComponent } from './transferencias/historial.component';
import { TransferenciaFormComponent } from './transferencias/transferencia-form.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'cuentas' },
  { path: 'cuentas', component: CuentasComponent },
  { path: 'transferencias', component: TransferenciaFormComponent },
  { path: 'historial', component: HistorialComponent },
];
