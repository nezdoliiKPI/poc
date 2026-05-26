import { useCallback, useState } from 'react';
import { COLORS } from '../../theme';

// ── Toast ─────────────────────────────────────────────────────────────────────

interface Toast {
  id:   number;
  msg:  string;
  type: 'success' | 'error';
}

/**
 * Manages a timed list of toast notifications.
 * Each toast auto-dismisses after 3.5 seconds.
 */
export function useToast() {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const toast = useCallback((msg: string, type: 'success' | 'error' = 'success') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, msg, type }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 3500);
  }, []);

  return { toasts, toast };
}

/**
 * Renders the active toast stack in the bottom-right corner of the screen.
 */
export function ToastList({ toasts }: { toasts: Toast[] }) {
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

// ── SpinBox ───────────────────────────────────────────────────────────────────

/**
 * Integer input with custom ▲▼ increment/decrement buttons.
 * Native browser spinners are hidden via the .no-spinners CSS class.
 */
export function SpinBox({
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
          tabIndex={-1}
          onClick={() => onChange(Math.min(max, value + 1))}
          style={{
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            color: COLORS.textSecondary,
            fontSize: 9,
            padding: '3px 6px',
            lineHeight: 1,
            borderBottom: `1px solid ${COLORS.border}`,
          }}
        >
          ▲
        </button>
        <button
          tabIndex={-1}
          onClick={() => onChange(Math.max(min, value - 1))}
          style={{
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            color: COLORS.textSecondary,
            fontSize: 9,
            padding: '3px 6px',
            lineHeight: 1,
          }}
        >
          ▼
        </button>
      </div>
    </div>
  );
}

// ── Toggle ────────────────────────────────────────────────────────────────────

/**
 * Toggle switch for boolean (consume / optimize) fields.
 */
export function Toggle({
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

// ── TipIcon ───────────────────────────────────────────────────────────────────

/**
 * Help icon (?) that shows a tooltip popover on hover.
 */
export function TipIcon({ text }: { text: string }) {
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

// ── ThreshInput ───────────────────────────────────────────────────────────────

/**
 * Plain number input used in the thresholds form (no spinner buttons).
 * Accepts null to represent an empty / unset value.
 */
export function ThreshInput({
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

// ── ThreshRow ─────────────────────────────────────────────────────────────────

/**
 * Two-column row inside the threshold form — label on the left, input on the right.
 */
export function ThreshRow({ label, children }: { label: string; children: React.ReactNode }) {
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

// ── ApplyButton ───────────────────────────────────────────────────────────────

/**
 * Primary submit button shown at the bottom of each admin tab.
 * Displays a loading label and is disabled while a request is in flight.
 */
export function ApplyButton({
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
      className="hover-dark"
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
      }}
    >
      {loading ? 'Застосовую...' : label}
    </button>
  );
}

// ── TabFooter ─────────────────────────────────────────────────────────────────

/**
 * Footer strip that centres the apply button (and optional status text) below the form.
 */
export function TabFooter({ children }: { children: React.ReactNode }) {
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

// ── FormatBadge ───────────────────────────────────────────────────────────────

/**
 * Small badge indicating message encoding format (PROTO or JSON).
 */
export function FormatBadge({ format }: { format: 'PROTO' | 'JSON' }) {
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
