export function dinero(valor: number | null | undefined): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(valor ?? 0);
}

export function fechaHora(iso: string | null | undefined): string {
  if (!iso) {
    return '';
  }
  const [fecha, hora = ''] = iso.split('T');
  const [anio, mes, dia] = fecha.split('-');
  return `${dia}/${mes}/${anio} ${hora.substring(0, 5)}`;
}
