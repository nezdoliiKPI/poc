import type { TelemetryType } from './types';

export type MetricDef = {
  /** Ключ поля у точці даних (має збігатися з JSON-відповіддю API). */
  key: string;
  /** Назва метрики для людини. */
  label: string;
  /** Одиниця вимірювання (SI). Порожній рядок — без одиниці. */
  unit: string;
  /** Колір лінії/градієнта в графіку. */
  color: string;
  /** Скільки знаків після коми показувати. Якщо не задано — авто. */
  precision?: number;
};

export const METRICS_BY_TYPE: Record<TelemetryType, MetricDef[]> = {
  power: [
    { key: 'voltage', label: 'Напруга',     unit: 'V', color: '#5B9DFF', precision: 2 },
    { key: 'current', label: 'Струм',       unit: 'A', color: '#FFB454', precision: 3 },
    { key: 'power',   label: 'Потужність',  unit: 'W', color: '#5BD49C', precision: 1 },
  ],

  temperature: [
    { key: 'temperature', label: 'Температура', unit: '°C', color: '#FF6B6B', precision: 1 },
    { key: 'humidity',    label: 'Вологість',   unit: '%',  color: '#4ECDC4', precision: 1 },
  ],

  'air-quality': [
    { key: 'co2',         label: 'CO\u2082',    unit: 'ppm',   color: '#FFC857', precision: 0 },
    { key: 'pm25',        label: 'PM2.5',        unit: 'µg/m³', color: '#E76F51', precision: 1 },
    { key: 'pm10',        label: 'PM10',         unit: 'µg/m³', color: '#F4A261', precision: 1 },
    { key: 'tvoc',        label: 'TVOC',         unit: 'mg/m³', color: '#A06CD5', precision: 2 },
    { key: 'temperature', label: 'Температура',  unit: '°C',    color: '#FF6B6B', precision: 1 },
    { key: 'humidity',    label: 'Вологість',    unit: '%',     color: '#4ECDC4', precision: 1 },
  ],

  smoke: [
    { key: 'smokeRaw', label: 'Рівень диму (raw)', unit: '',    color: '#9CA3AF', precision: 0 },
    { key: 'coLevel',  label: 'CO',                 unit: 'ppm', color: '#E76F51', precision: 0 },
  ],

  battery: [
    { key: 'val', label: 'Напруга батареї', unit: '%', color: '#5BD49C', precision: 2 },
  ],
};

export const TIME_WINDOWS = [
  { label: '30 с',    minutes: 0.5 },
  { label: '5 хв',    minutes: 5 },
  { label: '15 хв',   minutes: 15 },
  { label: '1 год',   minutes: 60 },
  { label: '6 год',   minutes: 360 },
  { label: '24 год',  minutes: 1440 },
  { label: '7 днів',   minutes: 7 * 1440 },
] as const;
