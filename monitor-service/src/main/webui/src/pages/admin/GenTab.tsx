import { useState } from 'react';
import { COLORS } from '../../theme';
import { updateProducerConfig, type ProducerConfig } from '../../api/admin';
import {
  SpinBox, ApplyButton, TabFooter, ToastList, useToast, FormatBadge,
} from './shared';

// ── Types and constants ────────────────────────────────────────────────────────

type GenKey = keyof ProducerConfig;

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

const DEFAULT_GEN: ProducerConfig = {
  powerProtoCount: 1, powerJsonCount: 1,
  smokeProtoCount: 1, smokeJsonCount: 1,
  airProtoCount:   1, airJsonCount:   1,
  tempProtoCount:  1, tempJsonCount:  1,
};

// ── Component ─────────────────────────────────────────────────────────────────

/**
 * Tab that controls the data-generator message rate per topic and format.
 * Posts the config to /api/producer/update/gen and shows the resulting msg/s rate.
 */
export function GenTab() {
  const [gen, setGen]           = useState<ProducerConfig>(DEFAULT_GEN);
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
              fontSize: 13,
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
            borderBottom: gi < GEN_ROWS.length - 1 ? `1px solid ${COLORS.border}` : undefined,
          }}
        >
          {group.map((row) => (
            <div
              key={row.key}
              className="hover-row"
              style={{
                display: 'grid',
                gridTemplateColumns: '2fr 2fr 148px',
                alignItems: 'center',
                padding: '10px 0',
              }}
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
              <div style={{ fontFamily: 'monospace', fontSize: 12, color: COLORS.textSecondary }}>
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
