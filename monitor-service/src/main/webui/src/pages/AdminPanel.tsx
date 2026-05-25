import { useEffect, useMemo, useState, useCallback } from 'react';
import { COLORS } from '../theme';
import { getDevices } from '../api/devices';
import type { Device } from '../types';
import {
  updateProducerConfig,
  updateEdgeConfig,
  updateThresholds,
  type ThresholdType,
} from '../api/admin';

// ── Shared toast hook ─────────────────────────────────────────────────────────

interface Toast { id: number; msg: string; type: 'success' | 'error' }

function useToast() {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const toast = useCallback((msg: string, type: 'success' | 'error' = 'success') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, msg, type }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 3500);
  }, []);

  return { toasts, toast };
}

// ── Shared UI components ──────────────────────────────────────────────────────

function ToastList({ toasts }: { toasts: Toast[] }) {
  if (!toasts.length) return null;
  return (
    <div style={{ position: 'fixed', bottom: 24, right: 24, zIndex: 999, display: 'flex', flexDirection: 'column', gap: 8 }}>
      {toasts.map((t) => (
        <div
          key={t.id}
          style={{
            padding: '13px 18px',
            borderRadius: 6,
            fontWeight: 500,
            fontSize: 13,
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            maxWidth: 340,
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            ...(t.type === 'success'
              ? { background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#166534' }
              : { background: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b' }),
          }}
        >
          <span style={{ flexShrink: 0 }}>{t.type === 'success' ? '✓' : '✕'}</span>
          <span>{t.msg}</span>
        </div>
      ))}
    </div>
  );
}

// Number input with ▲▼ buttons — matches the spinner style from the old panel.
function SpinBox({
  value,
  onChange,
  min = 0,
  max = 5000,
}: {
  value: number;
  onChange: (v: number) => void;
  min?: number;
  max?: number;
}) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        border: `1px solid ${COLORS.border}`,
        borderRadius: 4,
        background: COLORS.bgCard,
        overflow: 'hidden',
        width: 110,
      }}
    >
      <input
        type="number"
        className="no-spinners"
        value={value}
        min={min}
        max={max}
        onChange={(e) =>
          onChange(Math.min(max, Math.max(min, Number(e.target.value))))
        }
        style={{
          flex: 1,
          width: 0,
          background: 'transparent',
          border: 'none',
          outline: 'none',
          color: COLORS.textPrimary,
          fontFamily: 'monospace',
          fontSize: 14,
          fontWeight: 500,
          textAlign: 'center',
          padding: '6px 4px',
        }}
      />
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          background: COLORS.bgTableHead,
          borderLeft: `1px solid ${COLORS.border}`,
        }}
      >
        <button
          onClick={() => onChange(Math.min(max, value + 1))}
          style={{
            background: 'transparent',
            border: 'none',
            color: COLORS.textSecondary,
            cursor: 'pointer',
            padding: '2px 7px',
            fontSize: 9,
            lineHeight: 1,
          }}
        >
          ▲
        </button>
        <button
          onClick={() => onChange(Math.max(min, value - 1))}
          style={{
            background: 'transparent',
            border: 'none',
            color: COLORS.textSecondary,
            cursor: 'pointer',
            padding: '2px 7px',
            fontSize: 9,
            lineHeight: 1,
            borderTop: `1px solid ${COLORS.border}`,
          }}
        >
          ▼
        </button>
      </div>
    </div>
  );
}

// Toggle switch for boolean (consume / optimize) fields.
function Toggle({
  checked,
  onChange,
}: {
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <label
      style={{ position: 'relative', width: 36, height: 20, display: 'block', cursor: 'pointer' }}
    >
      <input
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        style={{ position: 'absolute', opacity: 0, width: 0, height: 0 }}
      />
      <div
        style={{
          position: 'absolute',
          inset: 0,
          borderRadius: 12,
          background: checked ? COLORS.accent : COLORS.border,
          transition: 'background 0.25s',
        }}
      />
      <div
        style={{
          position: 'absolute',
          top: 2,
          left: checked ? 18 : 2,
          width: 16,
          height: 16,
          borderRadius: '50%',
          background: 'white',
          boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
          transition: 'left 0.25s cubic-bezier(.4,0,.2,1)',
        }}
      />
    </label>
  );
}

// Tooltip help icon — shows a dark popover on hover.
function TipIcon({ text }: { text: string }) {
  const [show, setShow] = useState(false);
  return (
    <span style={{ position: 'relative', display: 'inline-flex', marginLeft: 5, verticalAlign: 'middle' }}>
      <span
        onMouseEnter={() => setShow(true)}
        onMouseLeave={() => setShow(false)}
        style={{
          width: 14,
          height: 14,
          borderRadius: '50%',
          background: COLORS.bgTableHead,
          border: `1px solid ${COLORS.border}`,
          color: COLORS.textSecondary,
          fontSize: 10,
          fontWeight: 600,
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'help',
        }}
      >
        ?
      </span>
      {show && (
        <div
          style={{
            position: 'absolute',
            bottom: 'calc(100% + 8px)',
            left: '50%',
            transform: 'translateX(-50%)',
            background: '#454a56',
            color: '#f8fafc',
            borderRadius: 6,
            padding: '9px 13px',
            width: 220,
            fontSize: 11.5,
            fontWeight: 400,
            lineHeight: 1.5,
            zIndex: 200,
            boxShadow: '0 10px 25px rgba(0,0,0,0.15)',
            whiteSpace: 'normal',
            textTransform: 'none',
            letterSpacing: 0,
            pointerEvents: 'none',
          }}
        >
          {text}
        </div>
      )}
    </span>
  );
}

// Plain number input used in the thresholds form (no spinner buttons).
function ThreshInput({
  value,
  onChange,
  step,
  placeholder,
}: {
  value: number | null;
  onChange: (v: number | null) => void;
  step?: number;
  placeholder?: string;
}) {
  return (
    <input
      type="number"
      step={step}
      value={value === null ? '' : value}
      placeholder={placeholder}
      onChange={(e) => {
        const val = e.target.value;
        onChange(val === '' ? null : Number(val));
      }}
      style={{
        width: '100%',
        background: COLORS.bgCard,
        border: `1px solid ${COLORS.border}`,
        borderRadius: 4,
        padding: '7px 10px',
        color: COLORS.textPrimary,
        fontFamily: 'monospace',
        fontSize: 13,
        outline: 'none',
      }}
    />
  );
}

// Row inside the threshold form — label on the left, input on the right.
function ThreshRow({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'minmax(180px, 1fr) 2fr',
        gap: 24,
        alignItems: 'center',
        maxWidth: 560,
        margin: '0 auto',
        padding: '9px 0',
      }}
    >
      <div style={{ fontSize: 13, fontWeight: 500, color: COLORS.textPrimary }}>
        {label}
      </div>
      <div>{children}</div>
    </div>
  );
}

// Apply / submit button at the bottom of each tab.
function ApplyButton({
  onClick,
  loading,
  disabled,
  label = 'ЗАСТОСУВАТИ',
}: {
  onClick: () => void;
  loading: boolean;
  disabled?: boolean;
  label?: string;
}) {
  const isDisabled = loading || !!disabled;
  return (
    <button
      onClick={onClick}
      disabled={isDisabled}
      style={{
        background: COLORS.textPrimary,
        border: 'none',
        borderRadius: 4,
        padding: '11px 48px',
        color: '#fff',
        fontSize: 13,
        fontWeight: 600,
        letterSpacing: '0.5px',
        cursor: isDisabled ? 'not-allowed' : 'pointer',
        opacity: isDisabled ? 0.5 : 1,
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        transition: 'background 0.2s',
      }}
      onMouseEnter={(e) => {
        if (!isDisabled) e.currentTarget.style.background = '#334155';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.background = COLORS.textPrimary;
      }}
    >
      {loading ? 'Застосовую...' : label}
    </button>
  );
}

// Shared footer that wraps the apply button and optional status line.
function TabFooter({ children }: { children: React.ReactNode }) {
  return (
    <div
      style={{
        padding: '20px 0 8px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 12,
        borderTop: `1px solid ${COLORS.border}`,
        marginTop: 8,
      }}
    >
      {children}
    </div>
  );
}

// Badge showing message format (PROTO or JSON).
function FormatBadge({ format }: { format: 'PROTO' | 'JSON' }) {
  const isProto = format === 'PROTO';
  return (
    <span
      style={{
        fontSize: 10,
        fontFamily: 'monospace',
        fontWeight: 600,
        padding: '2px 5px',
        borderRadius: 4,
        background: isProto ? 'rgba(59,130,246,0.1)' : 'rgba(14,165,233,0.1)',
        color: isProto ? '#2563eb' : '#0284c7',
      }}
    >
      {format}
    </span>
  );
}

// ── Generation tab ────────────────────────────────────────────────────────────

interface GenConfig {
  powerProtoCount: number;
  powerJsonCount:  number;
  smokeProtoCount: number;
  smokeJsonCount:  number;
  airProtoCount:   number;
  airJsonCount:    number;
  tempProtoCount:  number;
  tempJsonCount:   number;
}

type GenKey = keyof GenConfig;

interface GenRow {
  label:  string;
  format: 'PROTO' | 'JSON';
  topic:  string;
  key:    GenKey;
}

const GEN_ROWS: GenRow[][] = [
  [
    { label: 'Споживання енергії', format: 'PROTO', topic: 'dev/power/p',              key: 'powerProtoCount' },
    { label: 'Споживання енергії', format: 'JSON',  topic: 'dev/power/j',              key: 'powerJsonCount'  },
  ],
  [
    { label: 'Виявлення диму',     format: 'PROTO', topic: 'dev/smoke/p & dev/batt/p', key: 'smokeProtoCount' },
    { label: 'Виявлення диму',     format: 'JSON',  topic: 'dev/smoke/j & dev/batt/j', key: 'smokeJsonCount'  },
  ],
  [
    { label: 'Якість повітря',     format: 'PROTO', topic: 'dev/air/p & dev/batt/p',   key: 'airProtoCount'   },
    { label: 'Якість повітря',     format: 'JSON',  topic: 'dev/air/j & dev/batt/j',   key: 'airJsonCount'    },
  ],
  [
    { label: 'Температура',        format: 'PROTO', topic: 'dev/temp/p & dev/batt/p',  key: 'tempProtoCount'  },
    { label: 'Температура',        format: 'JSON',  topic: 'dev/temp/j & dev/batt/j',  key: 'tempJsonCount'   },
  ],
];

const DEFAULT_GEN: GenConfig = {
  powerProtoCount: 1, powerJsonCount: 1,
  smokeProtoCount: 1, smokeJsonCount: 1,
  airProtoCount:   1, airJsonCount:   1,
  tempProtoCount:  1, tempJsonCount:  1,
};

export function GenTab() {
  const [gen, setGen]           = useState<GenConfig>(DEFAULT_GEN);
  const [loading, setLoading]   = useState(false);
  const [totalMps, setTotalMps] = useState<number | null>(null);
  const { toasts, toast }       = useToast();

  const setField = (key: GenKey, value: number) =>
    setGen((prev) => ({ ...prev, [key]: value }));

  const apply = async () => {
    setLoading(true);
    try {
      const mps = await updateProducerConfig(gen);
      setTotalMps(mps);
      toast(`Налаштування застосовано — ${mps} пов/сек`, 'success');
    } catch (e) {
      toast((e as Error).message || 'Помилка мережі.', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* Column headers */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '2fr 2fr 148px',
          paddingBottom: 10,
          borderBottom: `1px solid ${COLORS.border}`,
          marginBottom: 4,
        }}
      >
        {(['Тип пристрою', 'Тема', 'Кількість пристроїв'] as const).map((h, i) => (
          <div
            key={h}
            style={{
              fontSize: 11,
              fontWeight: 600,
              textTransform: 'uppercase',
              letterSpacing: '0.5px',
              color: COLORS.textMuted,
              fontFamily: 'monospace',
              textAlign: i === 2 ? 'center' : 'left',
            }}
          >
            {h}
          </div>
        ))}
      </div>

      {/* Device type groups */}
      {GEN_ROWS.map((group, gi) => (
        <div
          key={gi}
          style={{
            borderBottom:
              gi < GEN_ROWS.length - 1 ? `1px solid ${COLORS.border}` : undefined,
          }}
        >
          {group.map((row) => (
            <div
              key={row.key}
              style={{
                display: 'grid',
                gridTemplateColumns: '2fr 2fr 148px',
                alignItems: 'center',
                padding: '10px 0',
              }}
              onMouseEnter={(e) =>
                (e.currentTarget.style.background = COLORS.bgRowHover)
              }
              onMouseLeave={(e) =>
                (e.currentTarget.style.background = 'transparent')
              }
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  fontSize: 13,
                  fontWeight: 500,
                  color: COLORS.textPrimary,
                }}
              >
                {row.label} <FormatBadge format={row.format} />
              </div>
              <div
                style={{ fontFamily: 'monospace', fontSize: 12, color: COLORS.textSecondary }}
              >
                {row.topic}
              </div>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <SpinBox value={gen[row.key]} onChange={(v) => setField(row.key, v)} />
              </div>
            </div>
          ))}
        </div>
      ))}

      <TabFooter>
        <ApplyButton onClick={apply} loading={loading} />
        {totalMps !== null && (
          <div style={{ fontFamily: 'monospace', fontSize: 13, color: COLORS.textSecondary }}>
            Всього генерується:{' '}
            <strong style={{ color: COLORS.accent }}>{totalMps} пов/сек</strong>
          </div>
        )}
      </TabFooter>

      <ToastList toasts={toasts} />
    </div>
  );
}

// ── Filter tab ────────────────────────────────────────────────────────────────

interface FilterRow {
  topic:     string;
  consume:   boolean;
  optimize:  boolean;
  threshold: number;
}

const DEFAULT_FILTER: FilterRow[][] = [
  [
    { topic: 'dev/power/p', consume: true, optimize: true, threshold: 0 },
    { topic: 'dev/power/j', consume: true, optimize: true, threshold: 0 },
  ],
  [
    { topic: 'dev/smoke/p', consume: true, optimize: true, threshold: 0 },
    { topic: 'dev/smoke/j', consume: true, optimize: true, threshold: 0 },
  ],
  [
    { topic: 'dev/air/p',   consume: true, optimize: true, threshold: 0 },
    { topic: 'dev/air/j',   consume: true, optimize: true, threshold: 0 },
  ],
  [
    { topic: 'dev/temp/p',  consume: true, optimize: true, threshold: 0 },
    { topic: 'dev/temp/j',  consume: true, optimize: true, threshold: 0 },
  ],
  [
    { topic: 'dev/batt/p',  consume: true, optimize: true, threshold: 0 },
    { topic: 'dev/batt/j',  consume: true, optimize: true, threshold: 0 },
  ],
];

const FILTER_COL_HEADERS = [
  { label: 'Тема',          tip: null,                                                                                                                     center: false },
  { label: 'Приймати',      tip: 'Якщо вимкнено, edge-вузол одразу відкидає всю вхідну телеметрію.',                                                       center: true  },
  { label: 'Оптимізувати',  tip: 'Відкидає надлишкову телеметрію нижче порогів чутливості для скорочення хмарного трафіку.',                               center: true  },
  { label: 'Поріг',         tip: 'Максимальна кількість незмінних повідомлень підряд, після якої примусово передається heartbeat.',                          center: true  },
];

export function FilterTab() {
  const [groups, setGroups]   = useState<FilterRow[][]>(DEFAULT_FILTER);
  const [loading, setLoading] = useState(false);
  const { toasts, toast }     = useToast();

  const updateRow = (gi: number, ri: number, patch: Partial<FilterRow>) => {
    setGroups((prev) => {
      const next = prev.map((g) => [...g]);
      next[gi][ri] = { ...next[gi][ri], ...patch };
      return next;
    });
  };

  const apply = async () => {
    setLoading(true);
    const failed: string[] = [];
    try {
      for (const group of groups) {
        for (const row of group) {
          try {
            await updateEdgeConfig({
              topic:     row.topic,
              consume:   row.consume,
              // When optimize is off, send threshold=0 so the edge drops nothing.
              threshold: row.optimize ? row.threshold : 0,
            });
          } catch (e) {
            failed.push(row.topic);
            toast(`${row.topic}: ${(e as Error).message}`, 'error');
          }
        }
      }
      if (failed.length === 0) {
        toast('Налаштування фільтрів застосовано.', 'success');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* Column headers */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '2fr 100px 120px 130px',
          paddingBottom: 10,
          borderBottom: `1px solid ${COLORS.border}`,
          marginBottom: 4,
        }}
      >
        {FILTER_COL_HEADERS.map(({ label, tip, center }) => (
          <div
            key={label}
            style={{
              fontSize: 11,
              fontWeight: 600,
              textTransform: 'uppercase',
              letterSpacing: '0.5px',
              color: COLORS.textMuted,
              fontFamily: 'monospace',
              display: 'flex',
              alignItems: 'center',
              justifyContent: center ? 'center' : 'flex-start',
            }}
          >
            {label}
            {tip && <TipIcon text={tip} />}
          </div>
        ))}
      </div>

      {/* Topic groups separated by thin borders */}
      {groups.map((group, gi) => (
        <div
          key={gi}
          style={{
            borderBottom:
              gi < groups.length - 1 ? `1px solid ${COLORS.border}` : undefined,
          }}
        >
          {group.map((row, ri) => (
            <div
              key={row.topic}
              style={{
                display: 'grid',
                gridTemplateColumns: '2fr 100px 120px 130px',
                alignItems: 'center',
                padding: '9px 0',
              }}
              onMouseEnter={(e) =>
                (e.currentTarget.style.background = COLORS.bgRowHover)
              }
              onMouseLeave={(e) =>
                (e.currentTarget.style.background = 'transparent')
              }
            >
              <div style={{ fontFamily: 'monospace', fontSize: 12, color: COLORS.textSecondary }}>
                {row.topic}
              </div>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <Toggle checked={row.consume}   onChange={(v) => updateRow(gi, ri, { consume: v })} />
              </div>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <Toggle checked={row.optimize}  onChange={(v) => updateRow(gi, ri, { optimize: v })} />
              </div>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <SpinBox value={row.threshold}  onChange={(v) => updateRow(gi, ri, { threshold: v })} />
              </div>
            </div>
          ))}
        </div>
      ))}

      <TabFooter>
        <ApplyButton onClick={apply} loading={loading} />
      </TabFooter>

      <ToastList toasts={toasts} />
    </div>
  );
}

// ── Thresholds tab ────────────────────────────────────────────────────────────

interface ThresholdForms {
  power:       { deviceId: number | null; minVoltage: number; maxVoltage: number; maxCurrent: number; maxPower: number };
  air:         { deviceId: number | null; maxCo2: number; maxPm25: number; maxPm10: number; maxTvoc: number; minTemperature: number; maxTemperature: number; minHumidity: number; maxHumidity: number };
  battery:     { deviceId: number | null; minBatteryLevel: number };
  smoke:       { deviceId: number | null; maxSmokeRaw: number; maxCoLevel: number };
  temperature: { deviceId: number | null; minTemperature: number; maxTemperature: number; minHumidity: number; maxHumidity: number };
}

const DEFAULT_THRESHOLDS: ThresholdForms = {
  power:       { deviceId: null, minVoltage: 190.0, maxVoltage: 250.0, maxCurrent: 16.0, maxPower: 3.5 },
  air:         { deviceId: null, maxCo2: 1000, maxPm25: 25.0, maxPm10: 50.0, maxTvoc: 0.5, minTemperature: 15.0, maxTemperature: 30.0, minHumidity: 30.0, maxHumidity: 60.0 },
  battery:     { deviceId: null, minBatteryLevel: 15.0 },
  smoke:       { deviceId: null, maxSmokeRaw: 300, maxCoLevel: 50 },
  temperature: { deviceId: null, minTemperature: 5.0, maxTemperature: 35.0, minHumidity: 20.0, maxHumidity: 80.0 },
};

const THRESHOLD_TYPE_LABELS: Record<ThresholdType, string> = {
  power:       'Споживання енергії',
  air:         'Якість повітря',
  battery:     'Акумулятор',
  smoke:       'Виявлення диму',
  temperature: 'Температура',
};

// Substring of the device topic that indicates compatibility with each threshold type.
// Battery is special: it matches devices that have a batteryTopic OR a 'batt' primary topic.
const TOPIC_PATTERNS: Record<ThresholdType, string> = {
  power:       'power',
  air:         'air',
  smoke:       'smoke',
  temperature: 'temp',
  battery:     'batt',
};

interface DeviceValidation {
  ok:  boolean;
  msg: string;
}

// Checks whether a device with the given ID can produce data for the threshold type.
function validateDevice(
  deviceId: number | null,
  type: ThresholdType,
  devices: Device[],
): DeviceValidation | null {
  if (deviceId === null) return null;    // global threshold — always valid
  if (devices.length === 0) return null; // still loading, skip for now

  const device = devices.find((d) => d.id === deviceId);
  if (!device) {
    return { ok: false, msg: `Пристрій #${deviceId} не знайдено серед зареєстрованих пристроїв.` };
  }

  const pattern = TOPIC_PATTERNS[type];
  // Battery thresholds also apply to devices that send battery data via their batteryTopic.
  const matches =
    type === 'battery'
      ? device.batteryTopic !== null || device.topic.includes(pattern)
      : device.topic.includes(pattern);

  if (!matches) {
    return {
      ok:  false,
      msg: `Пристрій #${deviceId} (тема: ${device.topic}) не підходить для порогів «${THRESHOLD_TYPE_LABELS[type]}». Очікується тема, що містить «${pattern}».`,
    };
  }

  return {
    ok:  true,
    msg: `✓ Пристрій #${deviceId} (${device.hardwareId}) відповідає типу «${THRESHOLD_TYPE_LABELS[type]}».`,
  };
}

export function ThresholdsTab() {
  const [thresholds, setThresholds] = useState<ThresholdForms>(DEFAULT_THRESHOLDS);
  const [type, setType]             = useState<ThresholdType>('power');
  const [loading, setLoading]       = useState(false);
  const [devices, setDevices]       = useState<Device[]>([]);
  const { toasts, toast }           = useToast();

  // Load all devices once so we can validate the device ID against them.
  useEffect(() => {
    getDevices().then(setDevices).catch(() => {});
  }, []);

  // Re-run validation whenever the device ID or threshold type changes.
  const validation = useMemo(
    () => validateDevice(thresholds[type].deviceId, type, devices),
    [thresholds, type, devices],
  );

  // Generic field setter for any threshold type.
  const setField = <K extends ThresholdType>(
    t: K,
    field: keyof ThresholdForms[K],
    value: ThresholdForms[K][typeof field],
  ) => {
    setThresholds((prev) => ({ ...prev, [t]: { ...prev[t], [field]: value } }));
  };

  const apply = async () => {
    if (validation?.ok === false) {
      toast('Виправте помилку перевірки ID пристрою перед збереженням.', 'error');
      return;
    }

    const current = thresholds[type];
    const { deviceId, ...fields } = current as { deviceId: number | null; [key: string]: unknown };

    let payloads: object[];

    if (deviceId === null) {
      // Global mode: create one threshold entry per matching device.
      if (devices.length === 0) {
        toast('Список пристроїв ще завантажується — зачекайте та спробуйте знову.', 'error');
        return;
      }
      const pattern = TOPIC_PATTERNS[type];
      const matched = devices.filter((d) =>
        type === 'battery'
          ? d.batteryTopic !== null || d.topic.includes(pattern)
          : d.topic.includes(pattern),
      );
      if (matched.length === 0) {
        toast(
          `Не знайдено пристроїв типу «${THRESHOLD_TYPE_LABELS[type]}» для глобального налаштування.`,
          'error',
        );
        return;
      }
      payloads = matched.map((d) => ({ ...fields, deviceId: d.id }));
    } else {
      // Specific device: send a single entry with the given deviceId.
      payloads = [current];
    }

    setLoading(true);
    try {
      await updateThresholds(type, payloads);
      toast(
        deviceId === null
          ? `Пороги «${THRESHOLD_TYPE_LABELS[type]}» оновлено для ${payloads.length} пристроїв.`
          : `Пороги «${THRESHOLD_TYPE_LABELS[type]}» оновлено для пристрою #${deviceId}.`,
        'success',
      );
    } catch (e) {
      toast((e as Error).message || 'Помилка мережі.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const pw  = thresholds.power;
  const air = thresholds.air;
  const bat = thresholds.battery;
  const smk = thresholds.smoke;
  const tmp = thresholds.temperature;

  return (
    <div>
      {/* Type selector */}
      <div style={{ marginBottom: 24 }}>
        <select
          value={type}
          onChange={(e) => setType(e.target.value as ThresholdType)}
          style={{
            background: COLORS.bgCard,
            border: `1px solid ${COLORS.border}`,
            borderRadius: 4,
            padding: '8px 36px 8px 12px',
            color: COLORS.textPrimary,
            fontSize: 13,
            fontWeight: 500,
            outline: 'none',
            cursor: 'pointer',
            appearance: 'none',
            backgroundImage: `url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%235a6680%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E")`,
            backgroundRepeat: 'no-repeat',
            backgroundPosition: 'right 12px top 50%',
            backgroundSize: '10px auto',
            minWidth: 200,
          }}
        >
          {(Object.keys(THRESHOLD_TYPE_LABELS) as ThresholdType[]).map((t) => (
            <option key={t} value={t}>{THRESHOLD_TYPE_LABELS[t]}</option>
          ))}
        </select>
      </div>

      {/* Column headers */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(180px, 1fr) 2fr',
          gap: 24,
          maxWidth: 560,
          margin: '0 auto',
          paddingBottom: 10,
          borderBottom: `1px solid ${COLORS.border}`,
          marginBottom: 4,
        }}
      >
        {['Параметр', 'Значення'].map((h) => (
          <div
            key={h}
            style={{
              fontSize: 11,
              fontWeight: 600,
              textTransform: 'uppercase',
              letterSpacing: '0.5px',
              color: COLORS.textMuted,
              fontFamily: 'monospace',
            }}
          >
            {h}
          </div>
        ))}
      </div>

      {/* Device ID row with inline validation feedback */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(180px, 1fr) 2fr',
          gap: 24,
          alignItems: 'start',
          maxWidth: 560,
          margin: '0 auto',
          padding: '9px 0',
        }}
      >
        <div style={{ fontSize: 13, fontWeight: 500, color: COLORS.textPrimary, paddingTop: 7 }}>
          ID пристрою
        </div>
        <div>
          <ThreshInput
            value={thresholds[type].deviceId}
            onChange={(v) => setField(type, 'deviceId', v)}
            placeholder="глобально"
          />
          {/* Validation message appears only when a specific device ID is entered. */}
          {validation && (
            <div
              style={{
                marginTop: 6,
                fontSize: 12,
                color: validation.ok ? '#166534' : COLORS.danger,
                fontFamily: 'monospace',
                lineHeight: 1.4,
              }}
            >
              {validation.msg}
            </div>
          )}
        </div>
      </div>

      {/* Power fields */}
      {type === 'power' && (
        <>
          <ThreshRow label="Мін. напруга (В)">
            <ThreshInput value={pw.minVoltage}  onChange={(v) => setField('power', 'minVoltage', v ?? 0)}  step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. напруга (В)">
            <ThreshInput value={pw.maxVoltage}  onChange={(v) => setField('power', 'maxVoltage', v ?? 0)}  step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. струм (А)">
            <ThreshInput value={pw.maxCurrent}  onChange={(v) => setField('power', 'maxCurrent', v ?? 0)}  step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. потужність (Вт)">
            <ThreshInput value={pw.maxPower}    onChange={(v) => setField('power', 'maxPower', v ?? 0)}    step={0.1} />
          </ThreshRow>
        </>
      )}

      {/* Air quality fields */}
      {type === 'air' && (
        <>
          <ThreshRow label="Макс. CO₂ (ppm)">
            <ThreshInput value={air.maxCo2}         onChange={(v) => setField('air', 'maxCo2', v ?? 0)} />
          </ThreshRow>
          <ThreshRow label="Макс. PM2.5 (мкг/м³)">
            <ThreshInput value={air.maxPm25}         onChange={(v) => setField('air', 'maxPm25', v ?? 0)}         step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. PM10 (мкг/м³)">
            <ThreshInput value={air.maxPm10}         onChange={(v) => setField('air', 'maxPm10', v ?? 0)}         step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. TVOC (мг/м³)">
            <ThreshInput value={air.maxTvoc}         onChange={(v) => setField('air', 'maxTvoc', v ?? 0)}         step={0.01} />
          </ThreshRow>
          <ThreshRow label="Мін. температура (°C)">
            <ThreshInput value={air.minTemperature}  onChange={(v) => setField('air', 'minTemperature', v ?? 0)}  step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. температура (°C)">
            <ThreshInput value={air.maxTemperature}  onChange={(v) => setField('air', 'maxTemperature', v ?? 0)}  step={0.1} />
          </ThreshRow>
          <ThreshRow label="Мін. вологість (%)">
            <ThreshInput value={air.minHumidity}     onChange={(v) => setField('air', 'minHumidity', v ?? 0)}     step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. вологість (%)">
            <ThreshInput value={air.maxHumidity}     onChange={(v) => setField('air', 'maxHumidity', v ?? 0)}     step={0.1} />
          </ThreshRow>
        </>
      )}

      {/* Battery fields */}
      {type === 'battery' && (
        <ThreshRow label="Мін. рівень заряду (%)">
          <ThreshInput value={bat.minBatteryLevel} onChange={(v) => setField('battery', 'minBatteryLevel', v ?? 0)} step={0.1} />
        </ThreshRow>
      )}

      {/* Smoke fields */}
      {type === 'smoke' && (
        <>
          <ThreshRow label="Макс. дим (raw)">
            <ThreshInput value={smk.maxSmokeRaw} onChange={(v) => setField('smoke', 'maxSmokeRaw', v ?? 0)} />
          </ThreshRow>
          <ThreshRow label="Макс. CO (ppm)">
            <ThreshInput value={smk.maxCoLevel}  onChange={(v) => setField('smoke', 'maxCoLevel', v ?? 0)} />
          </ThreshRow>
        </>
      )}

      {/* Temperature fields */}
      {type === 'temperature' && (
        <>
          <ThreshRow label="Мін. температура (°C)">
            <ThreshInput value={tmp.minTemperature} onChange={(v) => setField('temperature', 'minTemperature', v ?? 0)} step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. температура (°C)">
            <ThreshInput value={tmp.maxTemperature} onChange={(v) => setField('temperature', 'maxTemperature', v ?? 0)} step={0.1} />
          </ThreshRow>
          <ThreshRow label="Мін. вологість (%)">
            <ThreshInput value={tmp.minHumidity}    onChange={(v) => setField('temperature', 'minHumidity', v ?? 0)}    step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. вологість (%)">
            <ThreshInput value={tmp.maxHumidity}    onChange={(v) => setField('temperature', 'maxHumidity', v ?? 0)}    step={0.1} />
          </ThreshRow>
        </>
      )}

      <TabFooter>
        <ApplyButton
          onClick={apply}
          loading={loading}
          disabled={validation?.ok === false}
          label="ЗАДАТИ ПОРОГИ"
        />
      </TabFooter>

      <ToastList toasts={toasts} />
    </div>
  );
}
