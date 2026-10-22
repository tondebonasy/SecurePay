export function formatCurrency(amount: number, currency: string) {
  return `${amount.toFixed(2)} ${currency.toUpperCase()}`;
}

export function formatDate(iso?: string) {
  if (!iso) return "-";
  const d = new Date(iso);
  return d.toLocaleString();
}
