// Mirrors dev.nez.monitoring.model.Device
export type DeviceStatus =
  | 'ACTIVE'
  | 'MAINTENANCE'
  | 'BANNED'
  | 'DECOMMISSIONED';

export type MessageType = 'JSON' | 'PROTO';

export interface Device {
  id: number;
  hardwareId: string;
  status: DeviceStatus;
  messageType: MessageType;
  topic: string;
  batteryTopic: string | null;
}

// Supported telemetry data types
export type TelemetryType =
  | 'power'
  | 'temperature'
  | 'air-quality'
  | 'battery'
  | 'smoke';

// Mirrors dev.nez.monitoring.dto.PowerConsumptionPoint
export interface PowerConsumptionPoint {
  timeDate: string;
  deviceId: number;
  voltage: number;
  current: number;
  power: number;
}

// Mirrors dev.nez.monitoring.dto.TemperaturePoint
export interface TemperaturePoint {
  timeDate: string;
  deviceId: number;
  temperature: number;
  humidity: number;
}

// Mirrors dev.nez.monitoring.dto.AirQualityPoint
export interface AirQualityPoint {
  timeDate: string;
  deviceId: number;
  co2: number;
  pm25: number;
  pm10: number;
  tvoc: number;
  temperature: number;
  humidity: number;
}

// Mirrors dev.nez.monitoring.dto.BatteryPoint
export interface BatteryPoint {
  timeDate: string;
  deviceId: number;
  val: number;
}

// Mirrors dev.nez.monitoring.dto.SmokeDetectorPoint
export interface SmokeDetectorPoint {
  timeDate: string;
  deviceId: number;
  smokeRaw: number;
  coLevel: number;
}

export type AnyPoint =
  | PowerConsumptionPoint
  | TemperaturePoint
  | AirQualityPoint
  | BatteryPoint
  | SmokeDetectorPoint;

// Mirrors dev.nez.monitoring.dto.Alert
export type AlertSeverity = 'WARNING' | 'CRITICAL' | 'FAULT';

export interface Alert {
  uuid:   string;
  dID:    number;
  metric: string;
  val:    number;
  min:    number | null;
  max:    number | null;
  sev:    AlertSeverity;
  msg:    string;
  ts:     string; // ISO Instant
}

// Severity ordering for comparison (higher = worse)
export const SEVERITY_RANK: Record<AlertSeverity, number> = {
  WARNING:  1,
  CRITICAL: 2,
  FAULT:    3,
};
