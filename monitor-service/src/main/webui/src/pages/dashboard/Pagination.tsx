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

  const btnBase: React.CSSProperties = {
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

  const btnActive: React.CSSProperties = {
    ...btnBase,
    background: COLORS.accent,
    color: '#fff',
    borderColor: COLORS.accent,
    fontWeight: 600,
  };

  const btnDisabled: React.CSSProperties = {
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
        style={page === 1 ? btnDisabled : btnBase}
        disabled={page === 1}
        onClick={() => onChange(page - 1)}
        onMouseEnter={(e) => { if (page !== 1) e.currentTarget.style.background = COLORS.bgRowHover; }}
        onMouseLeave={(e) => { if (page !== 1) e.currentTarget.style.background = COLORS.bgCard; }}
        title="Попередня сторінка"
      >
        &lsaquo;
      </button>

      {/* First page + ellipsis */}
      {from > 1 && (
        <>
          <button
            style={btnBase}
            onClick={() => onChange(1)}
            onMouseEnter={(e) => (e.currentTarget.style.background = COLORS.bgRowHover)}
            onMouseLeave={(e) => (e.currentTarget.style.background = COLORS.bgCard)}
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
          style={p === page ? btnActive : btnBase}
          onClick={() => onChange(p)}
          onMouseEnter={(e) => { if (p !== page) e.currentTarget.style.background = COLORS.bgRowHover; }}
          onMouseLeave={(e) => { if (p !== page) e.currentTarget.style.background = COLORS.bgCard; }}
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
            style={btnBase}
            onClick={() => onChange(total)}
            onMouseEnter={(e) => (e.currentTarget.style.background = COLORS.bgRowHover)}
            onMouseLeave={(e) => (e.currentTarget.style.background = COLORS.bgCard)}
          >
            {total}
          </button>
        </>
      )}

      {/* › next */}
      <button
        style={page === total ? btnDisabled : btnBase}
        disabled={page === total}
        onClick={() => onChange(page + 1)}
        onMouseEnter={(e) => { if (page !== total) e.currentTarget.style.background = COLORS.bgRowHover; }}
        onMouseLeave={(e) => { if (page !== total) e.currentTarget.style.background = COLORS.bgCard; }}
        title="Наступна сторінка"
      >
        &rsaquo;
      </button>
    </div>
  );
}
