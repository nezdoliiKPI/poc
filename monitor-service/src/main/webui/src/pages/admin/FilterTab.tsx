import { useState } from 'react';
import { COLORS } from '../../theme';
import { updateEdgeConfig } from '../../api/admin';
import { SpinBox, Toggle, TipIcon, ApplyButton, TabFooter, ToastList, useToast } from './shared';

// ── Types and constants ────────────────────────────────────────────────────────

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
  { label: 'Тема',         tip: null,                                                                                                           center: false },
  { label: 'Приймати',     tip: 'Якщо вимкнено, edge-вузол одразу відкидає всю вхідну телеметрію.',                                            center: true  },
  { label: 'Оптимізувати', tip: 'Відкидає надлишкову телеметрію нижче порогів чутливості для скорочення хмарного трафіку.',                    center: true  },
  { label: 'Поріг',        tip: 'Максимальна кількість незмінних повідомлень підряд, після якої примусово передається heartbeat.',              center: true  },
];

// ── Component ─────────────────────────────────────────────────────────────────

/**
 * Tab for configuring edge-node topic filters.
 * Each row maps to a single POST /api/edge/update call; all rows are sent sequentially on apply.
 */
export function FilterTab() {
  const [groups, setGroups]   = useState<FilterRow[][]>(DEFAULT_FILTER);
  const [loading, setLoading] = useState(false);
  const { toasts, toast }     = useToast();

  /**
   * Applies a partial patch to one cell in the filter grid.
   */
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
              fontSize: 13,
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

      {/* Topic groups separated by thin dividers */}
      {groups.map((group, gi) => (
        <div
          key={gi}
          style={{
            borderBottom: gi < groups.length - 1 ? `1px solid ${COLORS.border}` : undefined,
          }}
        >
          {group.map((row, ri) => (
            <div
              key={row.topic}
              className="hover-row"
              style={{
                display: 'grid',
                gridTemplateColumns: '2fr 100px 120px 130px',
                alignItems: 'center',
                padding: '9px 0',
              }}
            >
              <div style={{ fontFamily: 'monospace', fontSize: 12, color: COLORS.textSecondary }}>
                {row.topic}
              </div>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <Toggle checked={row.consume}  onChange={(v) => updateRow(gi, ri, { consume: v })} />
              </div>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <Toggle checked={row.optimize} onChange={(v) => updateRow(gi, ri, { optimize: v })} />
              </div>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <SpinBox value={row.threshold} onChange={(v) => updateRow(gi, ri, { threshold: v })} />
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
