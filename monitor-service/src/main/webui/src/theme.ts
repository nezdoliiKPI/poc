// Central theme config. All components import colors and styles from here.

import type { DeviceStatus, TelemetryType } from './types';


export const COLORS = {
  // Page background — clean neutral base.
  bgPage:      '#f0f2f5',
  // Card and panel background.
  bgCard:      '#ffffff',
  // Table header and secondary panel background.
  bgTableHead: '#f7f8fa',
  // Row hover background.
  bgRowHover:  '#f3f5f8',

  border:       '#e2e5ea',
  borderStrong: '#c8ccd4',
  borderFocus:  '#3b82f6',

  textPrimary:   '#1a2030',
  textSecondary: '#5a6680',
  textMuted:     '#96a0b4',

  accent:      '#3b82f6',
  accentHover: '#2563eb',
  accentText:  '#ffffff',
  accentSubtle:'#eff6ff',

  danger:       '#dc2626',
  dangerBg:     '#fef2f2',
  dangerBorder: '#fecaca',
  dangerText:   '#991b1b',
} as const;


export interface StatusStyle {
  bg: string; text: string; border: string; label: string;
}

export const STATUS_STYLES: Record<DeviceStatus, StatusStyle> = {
  ACTIVE:         { bg: '#f0faf4', text: '#2d6a4f', border: '#c3e6cc', label: 'Активний'          },
  MAINTENANCE:    { bg: '#fffbeb', text: '#854d0e', border: '#fde68a', label: 'На обслуговуванні' },
  BANNED:         { bg: '#fff1f1', text: '#991b1b', border: '#fecaca', label: 'Заблокований'      },
  DECOMMISSIONED: { bg: '#f4f5f7', text: '#475569', border: '#d1d5db', label: 'Виведений'         },
};


export const MESSAGE_TYPE_LABELS: Record<string, string> = {
  JSON:  'JSON',
  PROTO: 'Protobuf',
};


export const CHART = {
  grid:    '#e8eaee',
  axis:    '#96a0b4',
  tooltip: {
    background:   '#1e2533',
    border:       '1px solid #3a4258',
    borderRadius: '6px',
    color:        '#e8ecf4',
    fontSize:     '12px',
  },
} as const;


export interface MetricDef {
  key: string; label: string; unit: string; color: string;
}

export const METRICS_BY_TYPE: Record<TelemetryType, MetricDef[]> = {
  power: [
    { key: 'voltage', label: 'Напруга',    unit: 'В',  color: '#4a7fc1' },
    { key: 'current', label: 'Струм',      unit: 'А',  color: '#3d9172' },
    { key: 'power',   label: 'Потужність', unit: 'Вт', color: '#b8893a' },
  ],
  temperature: [
    { key: 'temperature', label: 'Температура', unit: '°C', color: '#b85c5c' },
    { key: 'humidity',    label: 'Вологість',   unit: '%',  color: '#3d8fa8' },
  ],
  'air-quality': [
    { key: 'co2',         label: 'CO2',        unit: 'ppm',    color: '#6e5fa8' },
    { key: 'pm25',        label: 'PM2.5',      unit: 'мкг/м3', color: '#a85880' },
    { key: 'pm10',        label: 'PM10',       unit: 'мкг/м3', color: '#b87040' },
    { key: 'tvoc',        label: 'TVOC',       unit: 'мг/м3',  color: '#6a9230' },
    { key: 'temperature', label: 'Температура', unit: '°C',    color: '#b85c5c' },
    { key: 'humidity',    label: 'Вологість',   unit: '%',     color: '#3d8fa8' },
  ],
  smoke: [
    { key: 'smokeRaw', label: 'Дим',             unit: 'raw', color: '#7a8a9a' },
    { key: 'coLevel',  label: 'Чадний газ (CO)', unit: 'ppm', color: '#a84848' },
  ],
  battery: [
    { key: 'val', label: 'Заряд батареї', unit: '%', color: '#3d8f5c' },
  ],
};

// Available time window options for the history selector.
export const TIME_WINDOWS = [
  { label: '30 с',    minutes: 0.5   },
  { label: '5 хв',    minutes: 5     },
  { label: '15 хв',   minutes: 15    },
  { label: '1 год',   minutes: 60    },
  { label: '6 год',   minutes: 360   },
  { label: '24 год',  minutes: 1440  },
  { label: '7 днів',  minutes: 10080 },
] as const;
