import { apiFetch } from './client';

import type {
  PowerConsumptionPoint,
  TemperaturePoint,
  AirQualityPoint,
  BatteryPoint,
  SmokeDetectorPoint,
  TelemetryType,
} from '../types';

function buildUrl(
  deviceId: number,
  type: TelemetryType,
  from: Date,
  to: Date
): string {
  const params = new URLSearchParams({
    from: from.toISOString(),
    to: to.toISOString(),
  });
  return `/api/devices/${deviceId}/history/${type}?${params.toString()}`;
}

export const getPowerHistory = (id: number, from: Date, to: Date) =>
  apiFetch<PowerConsumptionPoint[]>(buildUrl(id, 'power', from, to));

export const getTemperatureHistory = (id: number, from: Date, to: Date) =>
  apiFetch<TemperaturePoint[]>(buildUrl(id, 'temperature', from, to));

export const getAirQualityHistory = (id: number, from: Date, to: Date) =>
  apiFetch<AirQualityPoint[]>(buildUrl(id, 'air-quality', from, to));

export const getBatteryHistory = (id: number, from: Date, to: Date) =>
  apiFetch<BatteryPoint[]>(buildUrl(id, 'battery', from, to));

export const getSmokeHistory = (id: number, from: Date, to: Date) =>
  apiFetch<SmokeDetectorPoint[]>(buildUrl(id, 'smoke', from, to));

import type { Alert } from '../types';

export const getAlertHistory = (id: number, from: Date, to: Date) =>
  apiFetch<Alert[]>(
    `/api/devices/${id}/history/alert?from=${from.toISOString()}&to=${to.toISOString()}`
  );

/**
 * Fetches alerts for multiple devices in a single request.
 * Uses /api/devices/0/history/alerts?deviceIds=1&deviceIds=2&...
 * The path id is ignored by the server; deviceIds query params carry the actual ids.
 */
export function getAlertsForDevices(ids: number[], from: Date, to: Date): Promise<Alert[]> {
  const params = new URLSearchParams({ from: from.toISOString(), to: to.toISOString() });
  ids.forEach((id) => params.append('deviceIds', String(id)));
  return apiFetch<Alert[]>(`/api/devices/0/history/alerts?${params.toString()}`);
}
