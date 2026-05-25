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
