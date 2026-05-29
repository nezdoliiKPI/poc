import { useEffect, useRef, useMemo, useState, type CSSProperties, type ReactNode } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getDevice } from '../../api/devices';
import { getBatteryHistory, getAlertHistory } from '../../api/history';
import { useSSEStream } from '../../hooks/useTelemetryStream';
import { useAlertStream } from '../../hooks/useAlertStream';
import { useError } from '../../hooks/useError';
import {
  COLORS, STATUS_STYLES, MESSAGE_TYPE_LABELS,
  METRICS_BY_TYPE, TIME_WINDOWS,
  type MetricDef,
} from '../../theme';
import { getTelemetryType } from '../../utils';
import type { Device, AnyPoint, BatteryPoint, Alert } from '../../types';
import {
  fmtFullDateTime, formatMetricValue,
  mergePoints, loadHistory,
} from './helpers';
import { MetricChart } from './MetricChart';
import { AlertList } from './AlertList';

// ── Small layout helpers ──────────────────────────────────────────────────────

/** Full-screen centered wrapper used for loading and error states. */
function Centered({ children, style }: { children: ReactNode; style?: CSSProperties }) {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, ...style }}>
      {children}
    </div>
  );
}

/** Placeholder card shown when there is no data at all (first load). */
function EmptyState({ children }: { children: ReactNode }) {
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

/** Suppress unused-variable warning — MetricDef is used only as a type constraint. */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
type _MetricDefUsed = MetricDef;

/**
 * Device detail page — shows device metadata, a sticky time-window selector,
 * a collapsible alert list, and area charts for each telemetry metric.
 */
export default function DeviceDetail() {
  const { id }    = useParams<{ id: string }>();
  const deviceId  = Number(id);
  const { showError } = useError();

  const [device,         setDevice]         = useState<Device | null>(null);
  const [windowMinutes,  setWindowMinutes]  = useState(5);
  const [historyPoints,  setHistoryPoints]  = useState<AnyPoint[]>([]);
  const [batteryHistory, setBatteryHistory] = useState<BatteryPoint[]>([]);
  const [alertHistory,   setAlertHistory]   = useState<Alert[]>([]);
  const [loadingDev,     setLoadingDev]     = useState(true);
  const [loadingData,    setLoadingData]    = useState(false);
  const [loadingAlerts,  setLoadingAlerts]  = useState(false);
  // Alerts panel is open by default when navigating from the Alerts tab (#alerts hash).
  const [alertsOpen,     setAlertsOpen]     = useState(() => window.location.hash === '#alerts');
  const alertsRef = useRef<HTMLDivElement>(null);

  // Tick every second so the X-axis domain slides smoothly rather than jumping.
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    const timerId = setInterval(() => setNow(Date.now()), 1_000);
    return () => clearInterval(timerId);
  }, []);

  // Load device metadata once.
  useEffect(() => {
    setLoadingDev(true);
    getDevice(deviceId)
      .then(setDevice)
      .catch((err) => showError(`Не вдалося завантажити пристрій: ${(err as Error).message}`))
      .finally(() => setLoadingDev(false));
  }, [deviceId, showError]);

  // Reload telemetry history on device or window change.
  // Old data is kept visible during the reload so the scroll position is preserved.
  useEffect(() => {
    if (!device) return;
    const type = getTelemetryType(device.topic);
    if (!type) return;

    const to   = new Date();
    const from = new Date(to.getTime() - windowMinutes * 60 * 1000);
    setLoadingData(true);

    const tasks: Promise<void>[] = [
      loadHistory(type, deviceId, from, to)
        .then((pts) => setHistoryPoints(pts ?? []))
        .catch((err) => showError(`Помилка телеметрії: ${(err as Error).message}`)),
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

  // Reload alert history on device or window change. Old data kept during reload.
  useEffect(() => {
    if (!device) return;
    const to   = new Date();
    const from = new Date(to.getTime() - windowMinutes * 60 * 1000);
    setLoadingAlerts(true);
    getAlertHistory(deviceId, from, to)
      .then((alerts) => setAlertHistory(alerts ?? []))
      .catch(() => {})
      .finally(() => setLoadingAlerts(false));
  }, [device?.id, deviceId, windowMinutes]);

  // Scroll to alerts section when arriving via #alerts hash.
  useEffect(() => {
    if (window.location.hash === '#alerts' && alertsRef.current) {
      setTimeout(() => alertsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 300);
    }
  }, [loadingDev]);

  // SSE streams.
  const windowMs      = windowMinutes * 60 * 1000;
  const telemetryType = device ? getTelemetryType(device.topic) : null;
  const hasBattery    = Boolean(device?.batteryTopic);

  const sseData    = useSSEStream<AnyPoint>(deviceId, telemetryType, windowMs);
  const sseBattery = useSSEStream<BatteryPoint>(deviceId, hasBattery ? 'battery' : null, windowMs);
  const sseAlerts  = useAlertStream(deviceId, windowMs);

  // Merge alert history and live SSE alerts, deduplicated by uuid.
  const allAlerts = useMemo(() => {
    const map = new Map<string, Alert>();
    [...alertHistory, ...sseAlerts].forEach((a) => map.set(a.uuid, a));
    return Array.from(map.values()).sort((a, b) => b.ts.localeCompare(a.ts));
  }, [alertHistory, sseAlerts]);

  // Merge telemetry history and live SSE points.
  const chartData    = useMemo(() => mergePoints(historyPoints, sseData, windowMs),    [historyPoints, sseData, windowMs]);
  const chartBattery = useMemo(() => mergePoints(batteryHistory, sseBattery, windowMs), [batteryHistory, sseBattery, windowMs]);

  // Fixed time-axis bounds shared by all charts so they stay in sync.
  const axisRange = useMemo(() => ({ from: now - windowMs, to: now }), [now, windowMs]);

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
    chartBattery.length > 0 ? (chartBattery[chartBattery.length - 1] as BatteryPoint).val : null;

  const metrics       = telemetryType ? METRICS_BY_TYPE[telemetryType] : [];
  const batteryMetric = METRICS_BY_TYPE.battery[0];

  // Show empty state only when there has truly never been any data (first load complete, nothing received).
  const hasNoData = !loadingData && historyPoints.length === 0 && sseData.length === 0;

  if (loadingDev) {
    return <Centered style={{ background: COLORS.bgPage, color: COLORS.textMuted }}>Завантаження...</Centered>;
  }
  if (!device) {
    return <Centered style={{ background: COLORS.bgPage, color: COLORS.dangerText }}>Пристрій не знайдено</Centered>;
  }

  const s = STATUS_STYLES[device.status];

  return (
    <div className="min-h-screen" style={{ background: 'transparent' }}>
      <div className="max-w-screen-xl mx-auto px-6 py-6 space-y-5">

        {/* Back link */}
        <div className="flex items-center gap-3">
          <Link
            to="/dashboard"
            className="hover-accent"
            style={{
              display: 'inline-flex', alignItems: 'center', gap: 5, fontSize: 13,
              fontWeight: 500, color: COLORS.textSecondary, textDecoration: 'none',
              padding: '5px 10px', borderRadius: 5, border: `1px solid ${COLORS.border}`,
              background: COLORS.bgCard, flexShrink: 0,
            }}
          >
            ← На головну
          </Link>
        </div>

        {/* Device info card */}
        <div className="rounded-lg p-5" style={{ background: COLORS.bgCard, border: `1px solid ${COLORS.border}` }}>
          <div className="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <h1 className="font-mono" style={{ fontSize: 22, fontWeight: 400, color: COLORS.textPrimary, lineHeight: 1.2 }}>
                {device.hardwareId}
              </h1>
              <p className="mt-1 text-xs" style={{ color: COLORS.textMuted }}>ID: {device.id}</p>
            </div>
            <span className="text-xs font-medium px-2.5 py-1 rounded"
              style={{ background: s.bg, color: s.text, border: `1px solid ${s.border}` }}>
              {s.label}
            </span>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4 pt-4"
            style={{ borderTop: `1px solid ${COLORS.border}` }}>
            <InfoField label="Формат даних" value={MESSAGE_TYPE_LABELS[device.messageType] ?? device.messageType} />
            <InfoField label="Тема" value={device.topic} mono />
            <InfoField label="Останнє повідомлення" value={loadingData ? '...' : (lastTime ?? 'Немає даних')} />
            <InfoField
              label="Заряд батареї"
              value={
                !hasBattery ? 'Відсутня' :
                latestBattery !== null ? formatMetricValue(latestBattery, batteryMetric) :
                (loadingData ? '...' : 'Немає даних')
              }
              accent={hasBattery && latestBattery !== null && latestBattery < 20 ? COLORS.danger : undefined}
            />
          </div>
        </div>

        {/* Sticky time window selector */}
        <div className="flex items-center gap-2"
          style={{ position: 'sticky', top: 0, zIndex: 10, background: COLORS.bgPage, padding: '8px 0' }}>
          <label htmlFor="time-window-select" className="text-xs uppercase tracking-wide"
            style={{ color: COLORS.textMuted }}>
            Період:
          </label>
          <select
            id="time-window-select"
            value={windowMinutes}
            onChange={(e) => setWindowMinutes(Number(e.target.value))}
            style={{
              padding: '4px 10px', borderRadius: 4, fontSize: 13,
              border: `1px solid ${COLORS.border}`, background: COLORS.bgCard,
              color: COLORS.textPrimary, cursor: 'pointer', outline: 'none',
            }}
            onFocus={(e) => (e.currentTarget.style.borderColor = COLORS.borderFocus)}
            onBlur={(e)  => (e.currentTarget.style.borderColor = COLORS.border)}
          >
            {TIME_WINDOWS.map(({ label, minutes }) => (
              <option key={minutes} value={minutes}>{label}</option>
            ))}
          </select>
          {loadingData && (
            <span style={{ fontSize: 12, color: COLORS.textMuted, marginLeft: 6 }}>
              Оновлення...
            </span>
          )}
        </div>

        {/* ── Collapsible alert list — above charts ─────────────────────────── */}
        <div ref={alertsRef} id="alerts" style={{ scrollMarginTop: 60 }}>
          {/* Clickable header */}
          <button
            onClick={() => setAlertsOpen((v) => !v)}
            style={{
              display: 'flex', alignItems: 'center', gap: 8, background: 'none',
              border: 'none', cursor: 'pointer', padding: '4px 0', marginBottom: alertsOpen ? 12 : 0,
            }}
          >
            <span style={{
              fontSize: 11, color: COLORS.textMuted, transition: 'transform 0.2s',
              display: 'inline-block', transform: alertsOpen ? 'rotate(90deg)' : 'rotate(0deg)',
            }}>
              ▶
            </span>
            <span style={{ fontSize: 13, fontWeight: 600, color: COLORS.textPrimary }}>
              Сповіщення
            </span>
            {allAlerts.length > 0 && (
              <span style={{ fontSize: 12, color: COLORS.textMuted }}>({allAlerts.length})</span>
            )}
          </button>

          {alertsOpen && <AlertList alerts={allAlerts} loading={loadingAlerts} />}
        </div>

        {/* ── Charts ────────────────────────────────────────────────────────── */}
        {hasNoData ? (
          <EmptyState>
            Даних за вибраний період немає.
            Очікування нових повідомлень від пристрою...
          </EmptyState>
        ) : (
          <>
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
