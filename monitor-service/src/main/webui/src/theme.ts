// Central theme config. All components import colours and styles from here.
// The base tone is cool grey.

import type { DeviceStatus, TelemetryType } from './types';


export const COLORS = {
  // Page background — the slightly darker base behind cards.
  bgPage:      '#e3e6eb',
  // Card and panel background — lighter than the page but not pure white.
  bgCard:      '#eef0f4',
  // Table header and secondary panel background.
  bgTableHead: '#e6e9ee',
  // Row hover background.
  bgRowHover:  '#e9ecf1',

  border:       '#cdd1d9',
  borderStrong: '#b4bac6',
  borderFocus:  '#2563eb',

  textPrimary:   '#1c2333',
  textSecondary: '#5a6680',
  textMuted:     '#8c97ad',

  accent:      '#2563eb',
  accentHover: '#1d4ed8',
  accentText:  '#ffffff',
  accentSubtle:'#dbeafe',   // subtle accent tint for active/selected UI elements

  danger:       '#dc2626',
  dangerBg:     '#fef2f2',
  dangerBorder: '#fecaca',
  dangerText:   '#991b1b',
} as const;


export interface StatusStyle {
  bg: string; text: string; border: string; label: string;
}

export const STATUS_STYLES: Record<DeviceStatus, StatusStyle> = {
  ACTIVE:         { bg: '#e6f4ea', text: '#2d6a4f', border: '#b7dfc7', label: 'Активний'       },
  MAINTENANCE:    { bg: '#fef9c3', text: '#854d0e', border: '#fde68a', label: 'На обслуговуванні' },
  BANNED:         { bg: '#fee2e2', text: '#991b1b', border: '#fecaca', label: 'Заблокований'   },
  DECOMMISSIONED: { bg: '#e9ecf1', text: '#475569', border: '#c8cdd7', label: 'Виведений'      },
};


export const MESSAGE_TYPE_LABELS: Record<string, string> = {
  JSON:  'JSON',
  PROTO: 'Protobuf',
};


export const CHART = {
  grid:    '#d8dce4',
  axis:    '#8c97ad',
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
    { key: 'voltage', label: 'Напруга',    unit: 'В',      color: '#2563eb' },
    { key: 'current', label: 'Струм',      unit: 'А',      color: '#10b981' },
    { key: 'power',   label: 'Потужність', unit: 'Вт',     color: '#f59e0b' },
  ],
  temperature: [
    { key: 'temperature', label: 'Температура', unit: '°C', color: '#ef4444' },
    { key: 'humidity',    label: 'Вологість',   unit: '%',  color: '#06b6d4' },
  ],
  'air-quality': [
    { key: 'co2',         label: 'CO₂',        unit: 'ppm',    color: '#8b5cf6' },
    { key: 'pm25',        label: 'PM2.5',       unit: 'мкг/м³', color: '#ec4899' },
    { key: 'pm10',        label: 'PM10',        unit: 'мкг/м³', color: '#f97316' },
    { key: 'tvoc',        label: 'TVOC',        unit: 'мг/м³',  color: '#84cc16' },
    { key: 'temperature', label: 'Температура', unit: '°C',     color: '#ef4444' },
    { key: 'humidity',    label: 'Вологість',   unit: '%',      color: '#06b6d4' },
  ],
  smoke: [
    { key: 'smokeRaw', label: '\u0414\u0438\u043c',             unit: 'raw', color: '#94a3b8' },
    { key: 'coLevel',  label: '\u0427\u0430\u0434\u043d\u0438\u0439 \u0433\u0430\u0437 (CO)', unit: 'ppm', color: '#dc2626' },
  ],
  battery: [
    { key: 'val', label: '\u0417\u0430\u0440\u044f\u0434 \u0431\u0430\u0442\u0430\u0440\u0435\u0457', unit: '%', color: '#22c55e' },
  ],
};

// Available time window options for the history selector.
export const TIME_WINDOWS = [
  { label: '30 \u0441',    minutes: 0.5   },
  { label: '5 \u0445\u0432',   minutes: 5     },
  { label: '15 \u0445\u0432',  minutes: 15    },
  { label: '1 \u0433\u043e\u0434',  minutes: 60    },
  { label: '6 \u0433\u043e\u0434',  minutes: 360   },
  { label: '24 \u0433\u043e\u0434', minutes: 1440  },
  { label: '7 \u0434\u043d\u0456\u0432', minutes: 10080 },
] as const;
