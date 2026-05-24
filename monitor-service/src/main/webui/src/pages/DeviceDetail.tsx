import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  AreaChart, Area, XAxis, YAxis,
  Tooltip, CartesianGrid, ResponsiveContainer,
} from 'recharts';

import { getDevice } from '../api/devices';
import {
  getPowerHistory, getTemperatureHistory,
  getAirQualityHistory, getBatteryHistory, getSmokeHistory,
} from '../api/history';
import { useSSEStream } from '../hooks/useTelemetryStream';
import { useError } from '../hooks/useError';
import {
  COLORS, STATUS_STYLES, MESSAGE_TYPE_LABELS,
  CHART, METRICS_BY_TYPE, TIME_WINDOWS,
  type MetricDef,
} from '../theme';
import { getTelemetryType } from '../utils';
import type { Device, AnyPoint, BatteryPoint, TelemetryType } from '../types';

const pad2 = (n: number) => n.toString().padStart(2, '0');

// Formats a timestamp as DD.MM.YYYY HH:MM:SS — used in tooltips and the "last message" field.
function fmtFullDateTime(ms: number): string {
  const d = new Date(ms);
  return `${pad2(d.getDate())}.${pad2(d.getMonth() + 1)}.${d.getFullYear()} ` +
         `${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`;
}

// Returns a tick label formatter for the X-axis.
// Short windows show seconds; medium windows drop to HH:MM; multi-day views add the date.
function makeTickFormatter(windowMinutes: number) {
  return (t: number) => {
    const d = new Date(t);
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

// Generates X-axis tick positions aligned to round boundaries (minute, 5 min, hour, day, etc.)
// based on the selected window size.
function generateTimeTicks(fromMs: number, toMs: number, windowMinutes: number): number[] {
  const minute = 60_000;
  const hour   = 60 * minute;
  const day    = 24 * hour;

  let step: number;
  if      (windowMinutes <= 0.5)       step = 5_000;         //  5 с  (30 с вікно  → ~6 міток)
  else if (windowMinutes <= 1)         step = 10_000;        // 10 с  (1 хв вікно  → ~6 міток)
  else if (windowMinutes <= 2)         step = 15_000;        // 15 с
  else if (windowMinutes <= 5)         step = minute;        //  1 хв
  else if (windowMinutes <= 15)        step = 2 * minute;    //  2 хв
  else if (windowMinutes <= 30)        step = 5 * minute;    //  5 хв
  else if (windowMinutes <= 60)        step = 10 * minute;   // 10 хв
  else if (windowMinutes <= 180)       step = 30 * minute;   // 30 хв
  else if (windowMinutes <= 360)       step = hour;          //  1 год
  else if (windowMinutes <= 720)       step = 2 * hour;      //  2 год
  else if (windowMinutes <= 1440)      step = 4 * hour;      //  4 год
  else if (windowMinutes <= 3 * 1440)  step = 12 * hour;     // 12 год
  else if (windowMinutes <= 7 * 1440)  step = day;           //  1 доба
  else                                  step = 7 * day;       //  7 діб

  const start = Math.ceil(fromMs / step) * step;
  const ticks: number[] = [];
  for (let t = start; t <= toMs; t += step) ticks.push(t);
  return ticks;
}

// Formats a metric value with the right number of decimal places and appends the unit.
// Uses the explicit precision from MetricDef if defined, otherwise picks precision by magnitude.
function formatMetricValue(val: unknown, metric: MetricDef): string {
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

function mergePoints<T extends AnyPoint>(
  history: T[],
  live: T[],
  windowMs: number,
): T[] {
  // History points are already bounded by the API query, so we don't re-filter them here.
  // That fixes a bug where network delay pushed edge points just outside the 30s window.
  // SSE live points do need filtering because they accumulate over time.
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

async function loadHistory(
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

export default function DeviceDetail() {
  const { id }   = useParams<{ id: string }>();
  const deviceId = Number(id);
  const { showError } = useError();

  const [device,         setDevice]         = useState<Device | null>(null);
  const [windowMinutes,  setWindowMinutes]  = useState(60);
  const [historyPoints,  setHistoryPoints]  = useState<AnyPoint[]>([]);
  const [batteryHistory, setBatteryHistory] = useState<BatteryPoint[]>([]);
  const [loadingDev,     setLoadingDev]     = useState(true);
  const [loadingData,    setLoadingData]    = useState(false);

  // Tick every second so the X-axis domain slides smoothly rather than jumping.
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1_000);
    return () => clearInterval(id);
  }, []);

  // Load the device metadata once, keyed by deviceId.
  useEffect(() => {
    setLoadingDev(true);
    getDevice(deviceId)
      .then(setDevice)
      .catch((err) =>
        showError(`Не вдалося завантажити пристрій: ${(err as Error).message}`),
      )
      .finally(() => setLoadingDev(false));
  }, [deviceId, showError]);

  // Reload telemetry history whenever the device or the selected time window changes.
  useEffect(() => {
    if (!device) return;

    const type = getTelemetryType(device.topic);
    if (!type) return;

    const to   = new Date();
    const from = new Date(to.getTime() - windowMinutes * 60 * 1000);

    setHistoryPoints([]);
    setBatteryHistory([]);
    setLoadingData(true);

    const tasks: Promise<void>[] = [
      loadHistory(type, deviceId, from, to)
        .then((pts) => setHistoryPoints(pts ?? []))
        .catch((err) =>
          showError(`Помилка телеметрії: ${(err as Error).message}`),
        ),
    ];

    if (device.batteryTopic) {
      tasks.push(
        getBatteryHistory(deviceId, from, to)
          .then((pts) => setBatteryHistory(pts ?? []))
          .catch(() => {}),
      );
    }

    Promise.all(tasks).finally(() => setLoadingData(false));
  }, [device?.id, device?.topic, device?.batteryTopic, deviceId, windowMinutes, showError]);

  // SSE streams deliver new points in real time; history is merged separately below.
  const windowMs       = windowMinutes * 60 * 1000;
  const telemetryType  = device ? getTelemetryType(device.topic) : null;
  const hasBattery     = Boolean(device?.batteryTopic);

  const sseData    = useSSEStream<AnyPoint>(deviceId, telemetryType, windowMs);
  const sseBattery = useSSEStream<BatteryPoint>(
    deviceId,
    hasBattery ? 'battery' : null,
    windowMs,
  );

  // Merge history and live SSE into a single deduplicated, sorted array for each chart.
  const chartData    = useMemo(
    () => mergePoints(historyPoints, sseData, windowMs),
    [historyPoints, sseData, windowMs],
  );
  const chartBattery = useMemo(
    () => mergePoints(batteryHistory, sseBattery, windowMs),
    [batteryHistory, sseBattery, windowMs],
  );

  // Fixed time-axis bounds shared by all charts so they stay in sync.
  const axisRange = useMemo(() => {
    const to = now;
    const from = to - windowMs;
    return { from, to };
  }, [now, windowMs]);

  // Derived display values.
  const lastTime: string | null = useMemo(() => {
    const all = [...historyPoints, ...sseData];
    if (all.length === 0) return null;
    const latest = all.reduce((best, p) =>
      new Date(p.timeDate).getTime() > new Date(best.timeDate).getTime() ? p : best
    );
    return fmtFullDateTime(new Date(latest.timeDate).getTime());
  }, [historyPoints, sseData]);

  const latestBattery: number | null =
    chartBattery.length > 0
      ? (chartBattery[chartBattery.length - 1] as BatteryPoint).val
      : null;

  const metrics = telemetryType ? METRICS_BY_TYPE[telemetryType] : [];
  const batteryMetric = METRICS_BY_TYPE.battery[0];

  const hasNoData = !loadingData && chartData.length === 0;

  if (loadingDev) {
    return (
      <Centered style={{ background: COLORS.bgPage, color: COLORS.textMuted }}>
        Завантаження...
      </Centered>
    );
  }

  if (!device) {
    return (
      <Centered style={{ background: COLORS.bgPage, color: COLORS.dangerText }}>
        Пристрій не знайдено
      </Centered>
    );
  }

  const s = STATUS_STYLES[device.status];

  return (
    <div className="min-h-screen" style={{ background: 'transparent' }}>
      <div className="max-w-5xl mx-auto px-6 py-6 space-y-5">

        {/* Картка пристрою */}
        <div
          className="rounded-lg p-5"
          style={{ background: COLORS.bgCard, border: `1px solid ${COLORS.border}` }}
        >
          <div className="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <h1 className="font-mono" style={{ fontSize: 22, fontWeight: 400, color: COLORS.textPrimary, lineHeight: 1.2 }}>
                {device.hardwareId}
              </h1>
              <p className="mt-1 text-xs" style={{ color: COLORS.textMuted }}>
                ID: {device.id}
              </p>
            </div>
            <span
              className="text-xs font-medium px-2.5 py-1 rounded"
              style={{ background: s.bg, color: s.text, border: `1px solid ${s.border}` }}
            >
              {s.label}
            </span>
          </div>

          <div
            className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4 pt-4"
            style={{ borderTop: `1px solid ${COLORS.border}` }}
          >
            <InfoField label="Формат даних"
              value={MESSAGE_TYPE_LABELS[device.messageType] ?? device.messageType} />
            <InfoField label="Тема" value={device.topic} mono />
            <InfoField
              label="Останнє повідомлення"
              value={loadingData ? '...' : (lastTime ?? 'Немає даних')}
            />
            {hasBattery && (
              <InfoField
                label="Заряд батареї"
                value={
                  latestBattery !== null
                    ? formatMetricValue(latestBattery, batteryMetric)
                    : (loadingData ? '...' : 'Немає даних')
                }
                accent={latestBattery !== null && latestBattery < 20
                  ? COLORS.danger : undefined}
              />
            )}
          </div>
        </div>

        {/* Вибір часового вікна */}
        <div className="flex items-center gap-2">
          <label
            htmlFor="time-window-select"
            className="text-xs uppercase tracking-wide"
            style={{ color: COLORS.textMuted }}
          >
            Період:
          </label>
          <select
            id="time-window-select"
            value={windowMinutes}
            onChange={(e) => setWindowMinutes(Number(e.target.value))}
            style={{
              padding: '4px 10px',
              borderRadius: 4,
              fontSize: 13,
              border: `1px solid ${COLORS.border}`,
              background: COLORS.bgCard,
              color: COLORS.textPrimary,
              cursor: 'pointer',
              outline: 'none',
            }}
            onFocus={(e) => (e.currentTarget.style.borderColor = COLORS.borderFocus)}
            onBlur={(e) => (e.currentTarget.style.borderColor = COLORS.border)}
          >
            {TIME_WINDOWS.map(({ label, minutes }) => (
              <option key={minutes} value={minutes}>{label}</option>
            ))}
          </select>
        </div>

        {/* Стан завантаження / відсутність даних */}
        {loadingData ? (
          <EmptyState>Завантаження даних...</EmptyState>
        ) : hasNoData ? (
          <EmptyState>
            Даних за вибраний період немає.
            Очікування нових повідомлень від пристрою...
          </EmptyState>
        ) : (
          <>
            {/* Графіки основної телеметрії */}
            {metrics.length > 0 && (
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
                {metrics.map((m) => (
                  <MetricChart
                    key={m.key}
                    data={chartData}
                    metric={m}
                    windowMinutes={windowMinutes}
                    fromMs={axisRange.from}
                    toMs={axisRange.to}
                  />
                ))}
              </div>
            )}

            {/* Графік батареї */}
            {hasBattery && chartBattery.length > 0 && (
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
                <MetricChart
                  data={chartBattery}
                  metric={batteryMetric}
                  windowMinutes={windowMinutes}
                  fromMs={axisRange.from}
                  toMs={axisRange.to}
                />
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}


function Centered({ children, style }: { children: React.ReactNode; style?: React.CSSProperties }) {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, ...style }}>
      {children}
    </div>
  );
}

function EmptyState({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ background: COLORS.bgCard, border: '1px solid ' + COLORS.border, borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '56px 24px', fontSize: 14, color: COLORS.textMuted, textAlign: 'center' }}>
      {children}
    </div>
  );
}

function InfoField({ label, value, mono, accent }: { label: string; value: string; mono?: boolean; accent?: string }) {
  return (
    <div>
      <div style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.05em', color: COLORS.textMuted, marginBottom: 2 }}>
        {label}
      </div>
      <div style={{ fontSize: 13, fontWeight: mono ? 400 : 500, fontFamily: mono ? 'monospace' : 'inherit', color: accent ?? COLORS.textPrimary }}>
        {value}
      </div>
    </div>
  );
}

// recharts needs a numeric X value, so we add _t (milliseconds) to every point.
type PlotPoint = AnyPoint & { _t: number };

function toPlotPoints(data: AnyPoint[]): PlotPoint[] {
  return data.map((p) => ({ ...p, _t: new Date(p.timeDate).getTime() }));
}

// Symmetric simple moving average — smooths noise before downsampling.
function applySMA(data: PlotPoint[], key: string, halfWin: number): PlotPoint[] {
  if (halfWin < 1) return data;
  type R = Record<string, number>;
  return data.map((p, i) => {
    const lo = Math.max(0, i - halfWin);
    const hi = Math.min(data.length - 1, i + halfWin);
    let sum = 0;
    for (let j = lo; j <= hi; j++) sum += (((data[j] as unknown) as R)[key] ?? 0);
    return { ...p, [key]: Math.round(sum / (hi - lo + 1) * 1000) / 1000 };
  });
}

// Largest-Triangle Three-Buckets downsampling (Steinarsson 2013).
// Picks the point in each bucket that maximises triangle area, preserving peaks and dips.
// Industry standard used in Grafana and Kibana.
function lttb(data: PlotPoint[], key: string, threshold: number): PlotPoint[] {
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

// Bucket size matching the backend resolveBucket() logic:
// ≤1 h → 1 min, ≤24 h → 5 min, ≤168 h → 1 h, >168 h → 1 day.
function getBucketMs(windowMinutes: number): number {
  const hours = windowMinutes / 60;
  if (hours <= 1)   return 60_000;
  if (hours <= 24)  return 5 * 60_000;
  if (hours <= 168) return 3_600_000;
  return 86_400_000;
}

function timeAggregate(data: PlotPoint[], key: string, bucketMs: number): PlotPoint[] {
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

// For windows ≥1 h use time-bucket aggregation to match the backend grouping.
// For shorter windows use SMA smoothing followed by LTTB downsampling.
function prepareChartData(data: PlotPoint[], key: string, windowMinutes: number, maxPts = 300): PlotPoint[] {
  if (data.length === 0) return data;
  if (windowMinutes >= 60) return timeAggregate(data, key, getBucketMs(windowMinutes));
  const density  = data.length / maxPts;
  const smoothed = density > 2 ? applySMA(data, key, Math.max(1, Math.round(density / 2))) : data;
  return lttb(smoothed, key, maxPts);
}
function MetricChart({
  data,
  metric,
  windowMinutes,
  fromMs,
  toMs,
}: {
  data: AnyPoint[];
  metric: MetricDef;
  windowMinutes: number;
  fromMs: number;
  toMs: number;
}) {
  const gradId  = `grad-${metric.key}`;
  const tickFmt = useMemo(() => makeTickFormatter(windowMinutes), [windowMinutes]);
  const ticks   = useMemo(
    () => generateTimeTicks(fromMs, toMs, windowMinutes),
    [fromMs, toMs, windowMinutes],
  );

  const plotData = useMemo(
    () => prepareChartData(toPlotPoints(data), metric.key, windowMinutes),
    [data, metric.key, windowMinutes],
  );

  const isEmpty = plotData.length === 0;

  return (
    <div style={{ background: COLORS.bgCard, border: '1px solid ' + COLORS.border, borderRadius: 8, padding: 16 }}>
      <h3 style={{ margin: '0 0 12px', fontSize: 13, fontWeight: 500, color: COLORS.textPrimary }}>
        {metric.label}
        {metric.unit && (
          <span style={{ marginLeft: 4, fontWeight: 400, color: COLORS.textMuted }}>
            ({metric.unit})
          </span>
        )}
      </h3>
      {isEmpty ? (
        <div style={{
          height: 200,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 13,
          color: COLORS.textMuted,
          border: `1px dashed ${COLORS.border}`,
          borderRadius: 6,
        }}>
          Немає даних за вибраний період
        </div>
      ) : (
      <ResponsiveContainer width="100%" height={200}>
        <AreaChart data={plotData} margin={{ top: 4, right: 8, left: -8, bottom: 0 }}>
          <defs>
            <linearGradient id={gradId} x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%"  stopColor={metric.color} stopOpacity={0.25} />
              <stop offset="95%" stopColor={metric.color} stopOpacity={0}    />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke={CHART.grid} />
          <XAxis
            dataKey="_t"
            type="number"
            scale="time"
            domain={[fromMs, toMs]}
            ticks={ticks}
            tickFormatter={tickFmt}
            stroke={CHART.axis}
            tick={{ fontSize: 10, fill: CHART.axis }}
            tickLine={false}
            minTickGap={36}
            allowDataOverflow
          />
          <YAxis
            stroke={CHART.axis}
            tick={{ fontSize: 10, fill: CHART.axis }}
            tickLine={false}
            axisLine={false}
            width={40}
          />
          <Tooltip
            contentStyle={CHART.tooltip}
            labelStyle={{ color: CHART.tooltip.color, marginBottom: 4 }}
            itemStyle={{ color: CHART.tooltip.color }}
            labelFormatter={(t: number) => fmtFullDateTime(t)}
            formatter={(val: number) => [formatMetricValue(val, metric), metric.label]}
          />
          <Area
            type="monotone"
            dataKey={metric.key}
            stroke={metric.color}
            strokeWidth={1.5}
            fill={`url(#${gradId})`}
            dot={false}
            isAnimationActive={false}
          />
        </AreaChart>
      </ResponsiveContainer>
      )}
    </div>
  );
}
