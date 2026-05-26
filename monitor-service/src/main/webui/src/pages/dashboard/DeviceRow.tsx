import { Link } from 'react-router-dom';
import { COLORS, STATUS_STYLES, MESSAGE_TYPE_LABELS } from '../../theme';
import type { Device } from '../../types';

/**
 * Renders a single device row inside the dashboard table.
 */
export function DeviceRow({ device: d }: { device: Device }) {
  const s = STATUS_STYLES[d.status];
  return (
    <tr
      className="hover-row"
      style={{ borderTop: `1px solid ${COLORS.border}` }}
    >
      <td className="px-4 py-3 text-sm" style={{ color: COLORS.textSecondary }}>
        {d.id}
      </td>
      <td className="px-4 py-3 font-mono text-sm" style={{ color: COLORS.textPrimary }}>
        {d.hardwareId}
      </td>
      <td className="px-4 py-3">
        <span
          className="text-xs font-medium px-2 py-0.5 rounded"
          style={{ background: s.bg, color: s.text, border: `1px solid ${s.border}` }}
        >
          {s.label}
        </span>
      </td>
      <td className="px-4 py-3 text-sm" style={{ color: COLORS.textSecondary }}>
        {MESSAGE_TYPE_LABELS[d.messageType] ?? d.messageType}
      </td>
      <td className="px-4 py-3 font-mono text-xs" style={{ color: COLORS.textMuted }}>
        {d.topic}
      </td>
      <td className="px-4 py-3 text-center">
        <Link
          to={`/devices/${d.id}`}
          className="hover-primary"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: 26,
            height: 26,
            borderRadius: 4,
            fontSize: 16,
            lineHeight: 1,
            border: `1px solid ${COLORS.border}`,
            color: COLORS.textSecondary,
            background: COLORS.bgCard,
            textDecoration: 'none',
          }}
        >
          &rsaquo;
        </Link>
      </td>
    </tr>
  );
}
