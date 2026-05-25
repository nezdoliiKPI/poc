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

// Mirrors dev.nez.monitoring.model.PowerConsumptionPoint
export interface PowerConsumptionPoint {
  timeDate: string;
  deviceId: number;
  voltage: number;
  current: number;
  power: number;
}

// Mirrors dev.nez.monitoring.model.TemperaturePoint
export interface TemperaturePoint {
  timeDate: string;
  deviceId: number;
  temperature: number;
  humidity: number;
}

// Mirrors dev.nez.monitoring.model.AirQualityPoint
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

// Mirrors dev.nez.monitoring.model.BatteryPoint
export interface BatteryPoint {
  timeDate: string;
  deviceId: number;
  val: number;
}

// Mirrors dev.nez.monitoring.model.SmokeDetectorPoint
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
