import type { TelemetryType } from './types';

/**
 * Infers the telemetry type from a device topic string.
 * Returns null if the topic does not match any known pattern.
 */
export function getTelemetryType(topic: string): TelemetryType | null {
  if (/\/power\//.test(topic))  return 'power';
  if (/\/smoke\//.test(topic))  return 'smoke';
  if (/\/air\//.test(topic))    return 'air-quality';
  if (/\/temp\//.test(topic))   return 'temperature';
  return null;
}

/** Formats a date string as a local HH:MM time. */
export const fmtTime = (t: string): string =>
  new Date(t).toLocaleTimeString('uk-UA', { hour: '2-digit', minute: '2-digit' });

/** Formats a date string as a full local date-time string. */
export const fmtDateTime = (t: string): string =>
  new Date(t).toLocaleString('uk-UA');

/** Returns the formatted timestamp of the last point in a telemetry array, or null if empty. */
export function lastMessageTime(points: { timeDate: string }[]): string | null {
  if (!points.length) return null;
  const last = points[points.length - 1].timeDate;
  return fmtDateTime(last);
}
