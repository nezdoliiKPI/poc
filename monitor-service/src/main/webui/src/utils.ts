import type { TelemetryType } from './types';

/**
 * Визначає тип телеметрії пристрою за рядком його топіку.
 * Повертає null якщо топік не розпізнано.
 */
export function getTelemetryType(topic: string): TelemetryType | null {
  if (/\/power\//.test(topic))  return 'power';
  if (/\/smoke\//.test(topic))  return 'smoke';
  if (/\/air\//.test(topic))    return 'air-quality';
  if (/\/temp\//.test(topic))   return 'temperature';
  return null;
}

/** Форматує рядок дати до локального часу HH:MM */
export const fmtTime = (t: string): string =>
  new Date(t).toLocaleTimeString('uk-UA', { hour: '2-digit', minute: '2-digit' });

/** Форматує рядок дати до повного локального формату */
export const fmtDateTime = (t: string): string =>
  new Date(t).toLocaleString('uk-UA');

/** Час з масиву точок телеметрії: повертає форматований рядок останньої точки або null */
export function lastMessageTime(points: { timeDate: string }[]): string | null {
  if (!points.length) return null;
  const last = points[points.length - 1].timeDate;
  return fmtDateTime(last);
}
