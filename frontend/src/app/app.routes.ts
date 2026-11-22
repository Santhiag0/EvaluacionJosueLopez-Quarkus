import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'cuentas' },
  {
    path: 'cuentas',
    loadComponent: () => import('./cuentas/cuentas.component').then((m) => m.CuentasComponent)
  },
  {
    path: 'transferir',
    loadComponent: () => import('./transferencia-form/transferencia-form.component').then((m) => m.TransferenciaFormComponent)
  },
  {
    path: 'historial',
    loadComponent: () => import('./historial/historial.component').then((m) => m.HistorialComponent)
  },
  { path: '**', redirectTo: 'cuentas' }
];
