import { useMemo, useState } from 'react';
import { COLORS } from '../../theme';
import type { Alert, AlertSeverity } from '../../types';
import { SEVERITY_RANK } from '../../types';
import { Pagination } from '../dashboard/Pagination';

const PAGE_SIZE = 10;

const SEV_STYLES: Record<AlertSeverity, { bg: string; text: string; border: string }> = {
  WARNING:  { bg: '#fffbeb', text: '#854d0e', border: '#fde68a' },
  CRITICAL: { bg: '#fff1f1', text: '#991b1b', border: '#fecaca' },
  FAULT:    { bg: '#f5f0ff', text: '#5b21b6', border: '#ddd6fe' },
};

type SortCol = 'ts' | 'sev' | 'metric';
type SortDir = 'asc' | 'desc';

interface Props {
  alerts: Alert[];
  loading: boolean;
}

/**
 * Sortable, paginated list of device alerts with expandable message on click.
 */
export function AlertList({ alerts, loading }: Props) {
  const [sort,        setSort]        = useState<{ col: SortCol; dir: SortDir }>({ col: 'ts', dir: 'desc' });
  const [page,        setPage]        = useState(1);
  const [expandedId,  setExpandedId]  = useState<string | null>(null);

  const handleSort = (col: SortCol) =>
    setSort((prev) => prev.col === col
      ? { col, dir: prev.dir === 'asc' ? 'desc' : 'asc' }
      : { col, dir: col === 'ts' ? 'desc' : 'asc' });

  const sorted = useMemo(() => [...alerts].sort((a, b) => {
    let cmp = 0;
    if (sort.col === 'ts')     cmp = a.ts.localeCompare(b.ts);
    if (sort.col === 'sev')    cmp = SEVERITY_RANK[a.sev] - SEVERITY_RANK[b.sev];
    if (sort.col === 'metric') cmp = a.metric.localeCompare(b.metric);
    return sort.dir === 'asc' ? cmp : -cmp;
  }), [alerts, sort]);

  const totalPages = Math.max(1, Math.ceil(sorted.length / PAGE_SIZE));
  const safePage   = Math.min(page, totalPages);
  const paged      = sorted.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const arrow = (col: SortCol) => sort.col === col
    ? <span style={{ marginLeft: 4, fontSize: 10 }}>{sort.dir === 'asc' ? '▲' : '▼'}</span>
    : null;

  const thStyle = (col: SortCol): React.CSSProperties => ({
    color: sort.col === col ? COLORS.textSecondary : COLORS.textMuted,
    cursor: 'pointer',
    userSelect: 'none',
    whiteSpace: 'nowrap',
    borderBottom: `1px solid ${COLORS.border}`,
  });

  return (
    <div style={{ border: `1px solid ${COLORS.border}`, borderRadius: 6, overflow: 'hidden' }}>
      <table className="w-full" style={{ tableLayout: 'fixed' }}>
        <colgroup>
          <col style={{ width: '15%' }} />
          <col style={{ width: '14%' }} />
          <col style={{ width: '14%' }} />
          <col style={{ width: '10%' }} />
          <col style={{ width: '10%' }} />
          <col />
        </colgroup>
        <thead>
          <tr style={{ background: COLORS.bgTableHead }}>
            <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
              style={thStyle('ts')} onClick={() => handleSort('ts')}>
              Час {arrow('ts')}
            </th>
            <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
              style={thStyle('sev')} onClick={() => handleSort('sev')}>
              Severity {arrow('sev')}
            </th>
            <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
              style={thStyle('metric')} onClick={() => handleSort('metric')}>
              Метрика {arrow('metric')}
            </th>
            <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
              style={{ color: COLORS.textMuted, borderBottom: `1px solid ${COLORS.border}`, whiteSpace: 'nowrap' }}>
              Значення
            </th>
            <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
              style={{ color: COLORS.textMuted, borderBottom: `1px solid ${COLORS.border}`, whiteSpace: 'nowrap' }}>
              Діапазон
            </th>
            <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
              style={{ color: COLORS.textMuted, borderBottom: `1px solid ${COLORS.border}` }}>
              Повідомлення
            </th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={6} className="text-center py-8 text-sm" style={{ color: COLORS.textMuted }}>
                Завантаження...
              </td>
            </tr>
          ) : paged.length === 0 ? (
            <tr>
              <td colSpan={6} className="text-center py-8 text-sm" style={{ color: COLORS.textMuted }}>
                Сповіщень за вибраний період немає
              </td>
            </tr>
          ) : (
            paged.map((a) => {
              const s   = SEV_STYLES[a.sev];
              const ts  = new Date(a.ts);
              const fmtTs = `${ts.toLocaleDateString('uk-UA')} ${ts.toLocaleTimeString('uk-UA', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}`;
              const isExpanded = expandedId === a.uuid;
              const range = (a.min !== null || a.max !== null)
                ? `${a.min ?? '—'} … ${a.max ?? '—'}`
                : '—';

              return (
                <>
                  <tr
                    key={a.uuid}
                    className="hover-row"
                    style={{ borderTop: `1px solid ${COLORS.border}`, cursor: 'pointer' }}
                    onClick={() => setExpandedId(isExpanded ? null : a.uuid)}
                  >
                    <td className="px-4 py-2.5 font-mono text-xs" style={{ color: COLORS.textMuted }}>
                      {fmtTs}
                    </td>
                    <td className="px-4 py-2.5">
                      <span className="text-xs font-medium px-2 py-0.5 rounded"
                        style={{ background: s.bg, color: s.text, border: `1px solid ${s.border}` }}>
                        {a.sev}
                      </span>
                    </td>
                    <td className="px-4 py-2.5 font-mono text-xs" style={{ color: COLORS.textPrimary }}>
                      {a.metric}
                    </td>
                    <td className="px-4 py-2.5 font-mono text-xs" style={{ color: COLORS.textSecondary }}>
                      {a.val}
                    </td>
                    <td className="px-4 py-2.5 font-mono text-xs" style={{ color: COLORS.textMuted }}>
                      {range}
                    </td>
                    <td className="px-4 py-2.5 text-xs" style={{ color: COLORS.textSecondary }}>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 10, color: COLORS.textMuted }}>{isExpanded ? '▲' : '▼'}</span>
                        <span style={{
                          maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                          display: 'inline-block',
                        }}>
                          {a.msg}
                        </span>
                      </span>
                    </td>
                  </tr>
                  {isExpanded && (
                    <tr key={`${a.uuid}-exp`} style={{ background: COLORS.bgTableHead }}>
                      <td colSpan={6} style={{ padding: '10px 16px', borderTop: `1px solid ${COLORS.border}` }}>
                        <div style={{
                          fontSize: 12, color: COLORS.textPrimary, lineHeight: 1.6,
                          fontFamily: 'monospace', whiteSpace: 'pre-wrap', wordBreak: 'break-word',
                        }}>
                          {a.msg}
                        </div>
                      </td>
                    </tr>
                  )}
                </>
              );
            })
          )}
        </tbody>
      </table>

      {!loading && totalPages > 1 && (
        <Pagination page={safePage} total={totalPages} onChange={(p) => { setPage(p); setExpandedId(null); }} count={sorted.length} />
      )}
    </div>
  );
}
