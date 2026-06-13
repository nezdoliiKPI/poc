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
 * Fetches alerts for multiple devices in a single request using the bulk endpoint.
 * The path {id} segment is required by JAX-RS routing but ignored by the server —
 * the actual device IDs are carried via the deviceIds query parameters.
 */
/**
 * Fetches alerts for multiple devices in one POST request.
 * Device IDs are sent in the JSON body to avoid URL length limits (3000+ devices).
 * Uses POST /api/devices/{id}/history/alerts — {id} is required by JAX-RS routing but ignored.
 */
export function getAlertsForDevices(ids: number[], from: Date, to: Date): Promise<Alert[]> {
  if (ids.length === 0) return Promise.resolve([]);

  return apiFetch<Alert[]>(
    `/api/devices/0/history/alerts?from=${from.toISOString()}&to=${to.toISOString()}`,
    { method: 'POST', body: JSON.stringify(ids) },
  );
}
