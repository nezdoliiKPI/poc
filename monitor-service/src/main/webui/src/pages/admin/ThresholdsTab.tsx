import { useEffect, useMemo, useState } from 'react';
import { getDevices } from '../../api/devices';
import type { Device } from '../../types';
import {
  updateThresholds,
  type ThresholdType,
} from '../../api/admin';
import {
  ThreshInput, ThreshRow, ApplyButton, TabFooter, ToastList, useToast,
} from './shared';
import { COLORS } from '../../theme';

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
// Battery is special: it also matches devices that report battery data via batteryTopic.
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

/**
 * Checks whether a device with the given ID can produce data for the threshold type.
 * Returns null when the check should be skipped (global mode or device list not yet loaded).
 */
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
      msg: `Пристрій ${device.hardwareId} (тема: ${device.topic}) не підходить для порогів «${THRESHOLD_TYPE_LABELS[type]}». Очікується тема, що містить «${pattern}».`,
    };
  }

  return {
    ok:  true,
    msg: `✓ Пристрій ${device.hardwareId} відповідає типу «${THRESHOLD_TYPE_LABELS[type]}».`,
  };
}

/**
 * Admin panel tab for configuring per-type alert thresholds.
 * When deviceId is null (global mode) the form expands to one payload per matching device.
 */
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
      // One payload per device — the backend requires explicit deviceId on each entry.
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
          : `Пороги «${THRESHOLD_TYPE_LABELS[type]}» оновлено для пристрою ID:${deviceId}.`,
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
            borderRadius: 6,
            padding: '8px 12px',
            color: COLORS.textPrimary,
            fontSize: 13,
            fontWeight: 500,
            outline: 'none',
            cursor: 'pointer',
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
              fontSize: 13,
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

      {type === 'air' && (
        <>
          <ThreshRow label="Макс. CO2 (ppm)">
            <ThreshInput value={air.maxCo2}         onChange={(v) => setField('air', 'maxCo2', v ?? 0)} />
          </ThreshRow>
          <ThreshRow label="Макс. PM2.5 (мкг/м3)">
            <ThreshInput value={air.maxPm25}         onChange={(v) => setField('air', 'maxPm25', v ?? 0)}         step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. PM10 (мкг/м3)">
            <ThreshInput value={air.maxPm10}         onChange={(v) => setField('air', 'maxPm10', v ?? 0)}         step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. TVOC (мг/м3)">
            <ThreshInput value={air.maxTvoc}         onChange={(v) => setField('air', 'maxTvoc', v ?? 0)}         step={0.01} />
          </ThreshRow>
          <ThreshRow label="Мін. температура (C)">
            <ThreshInput value={air.minTemperature}  onChange={(v) => setField('air', 'minTemperature', v ?? 0)}  step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. температура (C)">
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

      {type === 'battery' && (
        <ThreshRow label="Мін. рівень заряду (%)">
          <ThreshInput value={bat.minBatteryLevel} onChange={(v) => setField('battery', 'minBatteryLevel', v ?? 0)} step={0.1} />
        </ThreshRow>
      )}

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

      {type === 'temperature' && (
        <>
          <ThreshRow label="Мін. температура (C)">
            <ThreshInput value={tmp.minTemperature} onChange={(v) => setField('temperature', 'minTemperature', v ?? 0)} step={0.1} />
          </ThreshRow>
          <ThreshRow label="Макс. температура (C)">
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
