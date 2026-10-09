const pad = (n: number): string => String(n).padStart(2, '0');

/** Current local date and time in the format a datetime-local input expects. */
export function nowForDateTimeInput(): string {
  const d = new Date();
  return `${localDateString(d)}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** Local calendar date as YYYY-MM-DD (not UTC). */
export function localDateString(d: Date = new Date()): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
