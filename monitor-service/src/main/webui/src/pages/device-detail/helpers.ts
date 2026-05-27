import type { AnyPoint, TelemetryType } from '../../types';
import {
  getPowerHistory, getTemperatureHistory,
  getAirQualityHistory, getBatteryHistory, getSmokeHistory,
} from '../../api/history';

// ── Formatting helpers ────────────────────────────────────────────────────────

const pad2 = (n: number) => n.toString().padStart(2, '0');

/**
 * Formats a timestamp as DD.MM.YYYY HH:MM:SS — used in tooltips and the "last message" field.
 */
export function fmtFullDateTime(ms: number): string {
  const d = new Date(ms);
  return `${pad2(d.getDate())}.${pad2(d.getMonth() + 1)}.${d.getFullYear()} ` +
         `${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`;
}

/**
 * Returns a tick label formatter for the X-axis.
 * Short windows show seconds; medium windows drop to HH:MM; multi-day views add the date.
 */
export function makeTickFormatter(windowMinutes: number) {
  return (t: number) => {
    const d  = new Date(t);
    const hh = pad2(d.getHours());
    const mm = pad2(d.getMinutes());
    const dd = pad2(d.getDate());
    const mo = pad2(d.getMonth() + 1);

    if (windowMinutes <= 2)     return `${hh}:${mm}:${pad2(d.getSeconds())}`;
    if (windowMinutes <= 720)   return `${hh}:${mm}`;
    if (windowMinutes <= 10080) return `${dd}.${mo} ${hh}:${mm}`;
    return `${dd}.${mo}`;
  };
}

/**
 * Generates X-axis tick positions aligned to round boundaries (minute, 5 min, hour, day, etc.)
 * based on the selected window size.
 */
export function generateTimeTicks(fromMs: number, toMs: number, windowMinutes: number): number[] {
  const minute = 60_000;
  const hour   = 60 * minute;
  const day    = 24 * hour;

  let step: number;
  if      (windowMinutes <= 0.5)      step = 5_000;
  else if (windowMinutes <= 1)        step = 10_000;
  else if (windowMinutes <= 2)        step = 15_000;
  else if (windowMinutes <= 5)        step = minute;
  else if (windowMinutes <= 15)       step = 2 * minute;
  else if (windowMinutes <= 30)       step = 5 * minute;
  else if (windowMinutes <= 60)       step = 10 * minute;
  else if (windowMinutes <= 180)      step = 30 * minute;
  else if (windowMinutes <= 360)      step = hour;
  else if (windowMinutes <= 720)      step = 2 * hour;
  else if (windowMinutes <= 1440)     step = 4 * hour;
  else if (windowMinutes <= 3 * 1440) step = 12 * hour;
  else if (windowMinutes <= 7 * 1440) step = day;
  else                                step = 7 * day;

  const start = Math.ceil(fromMs / step) * step;
  const ticks: number[] = [];
  for (let t = start; t <= toMs; t += step) ticks.push(t);
  return ticks;
}

import type { MetricDef } from '../../theme';

/**
 * Formats a metric value with the right number of decimal places and appends the unit.
 * Uses the explicit precision from MetricDef if defined, otherwise picks precision by magnitude.
 */
export function formatMetricValue(val: unknown, metric: MetricDef): string {
  if (val == null || typeof val !== 'number' || Number.isNaN(val)) return '—';

  const explicit = (metric as MetricDef & { precision?: number }).precision;
  let p: number;
  if (typeof explicit === 'number') {
    p = explicit;
  } else {
    const abs = Math.abs(val);
    if      (abs >= 1000) p = 0;
    else if (abs >= 100)  p = 1;
    else if (abs >= 10)   p = 2;
    else                  p = 3;
  }

  const text = val.toFixed(p);
  return metric.unit ? `${text} ${metric.unit}` : text;
}

// ── Data merging ──────────────────────────────────────────────────────────────

/**
 * Merges history and live SSE point arrays into a single deduplicated, sorted array.
 * History points are not re-filtered (they're already bounded by the API query).
 * Live SSE points are filtered to the current window to prevent stale data accumulation.
 */
export function mergePoints<T extends AnyPoint>(
  history: T[],
  live: T[],
  windowMs: number,
): T[] {
  const cutoff = Date.now() - windowMs;
  const seen   = new Set<string>();
  const result: T[] = [];

  for (const p of history) {
    if (!seen.has(p.timeDate)) {
      seen.add(p.timeDate);
      result.push(p);
    }
  }

  for (const p of live) {
    const t = new Date(p.timeDate).getTime();
    if (t > cutoff && !seen.has(p.timeDate)) {
      seen.add(p.timeDate);
      result.push(p);
    }
  }

  return result.sort((a, b) => new Date(a.timeDate).getTime() - new Date(b.timeDate).getTime());
}

/**
 * Dispatches to the correct history API based on telemetry type.
 */
export async function loadHistory(
  type: TelemetryType,
  deviceId: number,
  from: Date,
  to: Date,
): Promise<AnyPoint[]> {
  switch (type) {
    case 'power':       return getPowerHistory(deviceId, from, to);
    case 'temperature': return getTemperatureHistory(deviceId, from, to);
    case 'air-quality': return getAirQualityHistory(deviceId, from, to);
    case 'smoke':       return getSmokeHistory(deviceId, from, to);
    default:            return [];
  }
}

// ── Chart processing ──────────────────────────────────────────────────────────

// recharts needs a numeric X value, so we add _t (milliseconds) to every point.
export type PlotPoint = AnyPoint & { _t: number };

/** Adds _t (epoch ms) to every point so recharts can use it as a numeric X value. */
export function toPlotPoints(data: AnyPoint[]): PlotPoint[] {
  return data.map((p) => ({ ...p, _t: new Date(p.timeDate).getTime() }));
}

/**
 * Largest-Triangle Three-Buckets downsampling (Steinarsson 2013).
 * Picks the point in each bucket that maximizes triangle area, preserving peaks and dips.
 * Industry standard used in Grafana and Kibana.
 */
export function lttb(data: PlotPoint[], key: string, threshold: number): PlotPoint[] {
  if (data.length <= threshold) return data;
  type R = Record<string, number>;
  const get = (p: PlotPoint) => (((p as unknown) as R)[key] ?? 0);

  const sampled: PlotPoint[] = [data[0]];
  const every = (data.length - 2) / (threshold - 2);
  let a = 0;

  for (let i = 0; i < threshold - 2; i++) {
    const nb0 = Math.floor((i + 1) * every) + 1;
    const nb1 = Math.min(Math.floor((i + 2) * every) + 1, data.length);
    let avgX = 0, avgY = 0;
    for (let j = nb0; j < nb1; j++) { avgX += data[j]._t; avgY += get(data[j]); }
    const cnt = nb1 - nb0;
    avgX /= cnt; avgY /= cnt;

    const cb0 = Math.floor(i * every) + 1;
    const cb1 = Math.min(Math.floor((i + 1) * every) + 1, data.length);
    const aX = data[a]._t, aY = get(data[a]);
    let maxArea = -1, pick = cb0;
    for (let j = cb0; j < cb1; j++) {
      const area = Math.abs((aX - avgX) * (get(data[j]) - aY) - (aX - data[j]._t) * (avgY - aY));
      if (area > maxArea) { maxArea = area; pick = j; }
    }
    sampled.push(data[pick]);
    a = pick;
  }
  sampled.push(data[data.length - 1]);
  return sampled;
}

/**
 * Bucket size matching the backend resolveBucket() logic:
 * <=1 h -> 1 min, <=24 h -> 5 min, <=168 h -> 1 h, >168 h -> 1 day.
 */
export function getBucketMs(windowMinutes: number): number {
  const hours = windowMinutes / 60;
  if (hours <= 1)   return 60_000;
  if (hours <= 24)  return 5 * 60_000;
  if (hours <= 168) return 3_600_000;
  return 86_400_000;
}

/** Averages data points into fixed-size time buckets, matching the backend aggregation. */
export function timeAggregate(data: PlotPoint[], key: string, bucketMs: number): PlotPoint[] {
  if (data.length === 0) return data;
  type R = Record<string, number>;
  const get = (p: PlotPoint) => (((p as unknown) as R)[key] ?? 0);
  const map = new Map<number, { sum: number; count: number; sample: PlotPoint }>();
  for (const p of data) {
    const bk = Math.floor(p._t / bucketMs) * bucketMs;
    const e = map.get(bk);
    if (e) { e.sum += get(p); e.count++; }
    else   { map.set(bk, { sum: get(p), count: 1, sample: p }); }
  }
  return Array.from(map.entries())
    .sort(([a], [b]) => a - b)
    .map(([bk, { sum, count, sample }]) => {
      const t   = bk + Math.floor(bucketMs / 2);
      const avg = Math.round(sum / count * 1000) / 1000;
      return { ...sample, _t: t, timeDate: new Date(t).toISOString(), [key]: avg };
    });
}

/**
 * For windows >=1 h use time-bucket aggregation to match the backend grouping.
 * For shorter windows use SMA smoothing followed by LTTB downsampling.
 */
export function prepareChartData(data: PlotPoint[], key: string, windowMinutes: number, maxPts = 300): PlotPoint[] {
  if (data.length === 0) return data;
  if (windowMinutes >= 60) return timeAggregate(data, key, getBucketMs(windowMinutes));
  return lttb(data, key, maxPts);
}

export { getBatteryHistory };
