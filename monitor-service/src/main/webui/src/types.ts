// Відповідає dev.nez.monitoring.model.Device
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

// Типи телеметрії
export type TelemetryType =
  | 'power'
  | 'temperature'
  | 'air-quality'
  | 'battery'
  | 'smoke';

// Відповідає PowerConsumptionPoint
export interface PowerConsumptionPoint {
  timeDate: string;
  deviceId: number;
  voltage: number;
  current: number;
  power: number;
}

// Відповідає TemperaturePoint
export interface TemperaturePoint {
  timeDate: string;
  deviceId: number;
  temperature: number;
  humidity: number;
}

// Відповідає AirQualityPoint
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

// Відповідає BatteryPoint
export interface BatteryPoint {
  timeDate: string;
  deviceId: number;
  val: number;
}

// Відповідає SmokeDetectorPoint
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
