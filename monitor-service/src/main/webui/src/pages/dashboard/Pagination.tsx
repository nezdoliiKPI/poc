import { type CSSProperties } from 'react';
import { COLORS } from '../../theme';

/**
 * Pagination bar — centered controls, arrow buttons, window of up to 5 page numbers.
 */
export function Pagination({
  page,
  total,
  onChange,
  count,
}: {
  page: number;
  total: number;
  onChange: (p: number) => void;
  count: number;
}) {
  // Show up to 5 page buttons centered around the current page.
  const range: number[] = [];
  const from = Math.max(1, page - 2);
  const to   = Math.min(total, from + 4);
  for (let i = from; i <= to; i++) range.push(i);

  const btnBase: CSSProperties = {
    border: `1px solid ${COLORS.border}`,
    background: COLORS.bgCard,
    color: COLORS.textSecondary,
    borderRadius: 4,
    minWidth: 30,
    height: 28,
    padding: '0 8px',
    fontSize: 13,
    cursor: 'pointer',
    transition: 'background 0.15s, color 0.15s, border-color 0.15s',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
  };

  const btnActive: CSSProperties = {
    ...btnBase,
    background: COLORS.accent,
    color: '#fff',
    borderColor: COLORS.accent,
    fontWeight: 600,
  };

  const btnDisabled: CSSProperties = {
    ...btnBase,
    opacity: 0.35,
    cursor: 'default',
  };

  return (
    <div
      style={{
        borderTop: `1px solid ${COLORS.border}`,
        padding: '10px 16px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 4,
        position: 'relative',
      }}
    >
      {/* Total count — absolute left so it doesn't shift the centered controls */}
      <span
        style={{
          position: 'absolute',
          left: 16,
          fontSize: 12,
          color: COLORS.textMuted,
        }}
      >
        Всього: {count}
      </span>

      {/* ‹ previous */}
      <button
        className="hover-row"
        style={page === 1 ? btnDisabled : btnBase}
        disabled={page === 1}
        onClick={() => onChange(page - 1)}
        title="Попередня сторінка"
      >
        &lsaquo;
      </button>

      {/* First page + ellipsis */}
      {from > 1 && (
        <>
          <button
            className="hover-row"
            style={btnBase}
            onClick={() => onChange(1)}
          >
            1
          </button>
          {from > 2 && (
            <span style={{ color: COLORS.textMuted, padding: '0 2px', fontSize: 13 }}>…</span>
          )}
        </>
      )}

      {/* Page number buttons */}
      {range.map((p) => (
        <button
          key={p}
          className={p !== page ? 'hover-row' : undefined}
          style={p === page ? btnActive : btnBase}
          onClick={() => onChange(p)}
        >
          {p}
        </button>
      ))}

      {/* Ellipsis + last page */}
      {to < total && (
        <>
          {to < total - 1 && (
            <span style={{ color: COLORS.textMuted, padding: '0 2px', fontSize: 13 }}>…</span>
          )}
          <button
            className="hover-row"
            style={btnBase}
            onClick={() => onChange(total)}
          >
            {total}
          </button>
        </>
      )}

      {/* › next */}
      <button
        className="hover-row"
        style={page === total ? btnDisabled : btnBase}
        disabled={page === total}
        onClick={() => onChange(page + 1)}
        title="Наступна сторінка"
      >
        &rsaquo;
      </button>
    </div>
  );
}
