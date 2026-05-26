import { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getDevice } from '../../api/devices';
import { getBatteryHistory } from '../../api/history';
import { useSSEStream } from '../../hooks/useTelemetryStream';
import { useError } from '../../hooks/useError';
import {
  COLORS, STATUS_STYLES, MESSAGE_TYPE_LABELS,
  METRICS_BY_TYPE, TIME_WINDOWS,
  type MetricDef,
} from '../../theme';
import { getTelemetryType } from '../../utils';
import type { Device, AnyPoint, BatteryPoint } from '../../types';
import {
  fmtFullDateTime, formatMetricValue,
  mergePoints, loadHistory,
} from './helpers';
import { MetricChart } from './MetricChart';

// ── Small layout helpers ──────────────────────────────────────────────────────

/** Full-screen centered wrapper used for loading and error states. */
function Centered({ children, style }: { children: React.ReactNode; style?: React.CSSProperties }) {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, ...style }}>
      {children}
    </div>
  );
}

/** Placeholder card shown while data is loading or when no data is available. */
function EmptyState({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ background: COLORS.bgCard, border: '1px solid ' + COLORS.border, borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '56px 24px', fontSize: 14, color: COLORS.textMuted, textAlign: 'center' }}>
      {children}
    </div>
  );
}

/** Label + value pair inside the device info card. */
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

// ── DeviceDetail ──────────────────────────────────────────────────────────────

/** Suppress unused-variable warning — DeviceStatus is used only as a type constraint. */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
type _MetricDefUsed = MetricDef;

/**
 * Device detail page — shows device metadata, a time-window selector,
 * and area charts for each telemetry metric streamed via SSE + REST history.
 */
export default function DeviceDetail() {
  const { id }    = useParams<{ id: string }>();
  const deviceId  = Number(id);
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
    const timerId = setInterval(() => setNow(Date.now()), 1_000);
    return () => clearInterval(timerId);
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
  const windowMs      = windowMinutes * 60 * 1000;
  const telemetryType = device ? getTelemetryType(device.topic) : null;
  const hasBattery    = Boolean(device?.batteryTopic);

  const sseData    = useSSEStream<AnyPoint>(deviceId, telemetryType, windowMs);
  const sseBattery = useSSEStream<BatteryPoint>(
    deviceId,
    hasBattery ? 'battery' : null,
    windowMs,
  );

  // Merge history and live SSE into a single deduplicated, sorted array for each chart.
  const chartData = useMemo(
    () => mergePoints(historyPoints, sseData, windowMs),
    [historyPoints, sseData, windowMs],
  );
  const chartBattery = useMemo(
    () => mergePoints(batteryHistory, sseBattery, windowMs),
    [batteryHistory, sseBattery, windowMs],
  );

  // Fixed time-axis bounds shared by all charts so they stay in sync.
  const axisRange = useMemo(() => {
    const to   = now;
    const from = to - windowMs;
    return { from, to };
  }, [now, windowMs]);

  // Most-recent timestamp across history + live points.
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

  const metrics       = telemetryType ? METRICS_BY_TYPE[telemetryType] : [];
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
      <div className="max-w-screen-xl mx-auto px-6 py-6 space-y-5">

        {/* Top nav row — back link + device title */}
        <div className="flex items-center gap-3">
          <Link
            to="/dashboard"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 5,
              fontSize: 13,
              fontWeight: 500,
              color: COLORS.textSecondary,
              textDecoration: 'none',
              padding: '5px 10px',
              borderRadius: 5,
              border: `1px solid ${COLORS.border}`,
              background: COLORS.bgCard,
              transition: 'color 0.15s, background 0.15s',
              flexShrink: 0,
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.color = COLORS.accent;
              e.currentTarget.style.background = COLORS.accentSubtle;
              e.currentTarget.style.borderColor = COLORS.accent;
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.color = COLORS.textSecondary;
              e.currentTarget.style.background = COLORS.bgCard;
              e.currentTarget.style.borderColor = COLORS.border;
            }}
          >
            ← На головну
          </Link>
        </div>

        {/* Device info card */}
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

        {/* Time window selector */}
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
            onBlur={(e)  => (e.currentTarget.style.borderColor = COLORS.border)}
          >
            {TIME_WINDOWS.map(({ label, minutes }) => (
              <option key={minutes} value={minutes}>{label}</option>
            ))}
          </select>
        </div>

        {/* Loading / empty state */}
        {/* Loading / empty state */}
        {loadingData ? (
          <EmptyState>Завантаження даних...</EmptyState>
        ) : hasNoData ? (
          <EmptyState>
            Даних за вибраний період немає.
            Очікування нових повідомлень від пристрою...
          </EmptyState>
        ) : (
          <>
            {/* Primary telemetry charts */}
            {metrics.length > 0 && (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
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

            {/* Battery chart */}
            {hasBattery && chartBattery.length > 0 && (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
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
