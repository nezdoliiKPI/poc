import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDevices } from '../api/devices';
import { useAuth } from '../hooks/useAuth';
import { useError } from '../hooks/useError';
import { COLORS, STATUS_STYLES, MESSAGE_TYPE_LABELS } from '../theme';
import type { Device, DeviceStatus } from '../types';

const PAGE_SIZE = 15;

type SortCol = 'id' | 'hardwareId' | 'status' | 'messageType' | 'topic';
type SortDir = 'asc' | 'desc';

const COLUMNS: { label: string; col: SortCol | null }[] = [
  { label: 'ID',           col: 'id'          },
  { label: 'Hardware ID',  col: 'hardwareId'  },
  { label: 'Статус',       col: 'status'      },
  { label: 'Формат даних', col: 'messageType' },
  { label: 'Тема',         col: 'topic'       },
  { label: '',             col: null          },
];

export default function Dashboard() {
  const { logout } = useAuth();
  const { showError } = useError();

  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [sort, setSort] = useState<{ col: SortCol; dir: SortDir }>({ col: 'id', dir: 'asc' });

  useEffect(() => {
    getDevices()
      .then(setDevices)
      .catch((err) => showError((err as Error).message))
      .finally(() => setLoading(false));
  }, [showError]);

  useEffect(() => { setPage(1); }, [search, sort]);

  const handleSort = (col: SortCol) => {
    setSort((prev) =>
      prev.col === col
        ? { col, dir: prev.dir === 'asc' ? 'desc' : 'asc' }
        : { col, dir: 'asc' },
    );
  };

  const filtered = devices.filter((d) =>
    d.hardwareId.toLowerCase().includes(search.toLowerCase())
  );

  const sorted = [...filtered].sort((a, b) => {
    const av = String(a[sort.col as keyof Device] ?? '');
    const bv = String(b[sort.col as keyof Device] ?? '');
    const cmp = sort.col === 'id'
      ? Number(a.id) - Number(b.id)
      : av.localeCompare(bv, 'uk');
    return sort.dir === 'asc' ? cmp : -cmp;
  });

  const totalPages = Math.max(1, Math.ceil(sorted.length / PAGE_SIZE));
  const safePage = Math.min(page, totalPages);
  const paged = sorted.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const stats = {
    total:          devices.length,
    active:         devices.filter((d) => d.status === 'ACTIVE').length,
    maintenance:    devices.filter((d) => d.status === 'MAINTENANCE').length,
    banned:         devices.filter((d) => d.status === 'BANNED').length,
    decommissioned: devices.filter((d) => d.status === 'DECOMMISSIONED').length,
  };

  return (
    <div className="min-h-screen" style={{ background: COLORS.bgPage }}>
      {/* Шапка */}
      <header
        className="flex items-center justify-between px-6 py-3"
        style={{
          background: COLORS.bgCard,
          borderBottom: `1px solid ${COLORS.border}`,
        }}
      >
        <span className="font-semibold text-base" style={{ color: COLORS.textPrimary }}>
          Моніторинг пристроїв
        </span>
        <button
          onClick={logout}
          className="text-sm px-4 py-1.5 rounded transition-colors"
          style={{
            border: `1px solid ${COLORS.border}`,
            color: COLORS.textSecondary,
            background: COLORS.bgCard,
          }}
          onMouseEnter={(e) => (e.currentTarget.style.background = COLORS.bgRowHover)}
          onMouseLeave={(e) => (e.currentTarget.style.background = COLORS.bgCard)}
        >
          Вийти
        </button>
      </header>

      <div className="max-w-5xl mx-auto px-6 py-6 space-y-6">
        {/* Статистика */}
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          <StatCard label="Всього"             value={stats.total}          />
          <StatCard label="Активні"            value={stats.active}         />
          <StatCard label="На обслуговуванні"  value={stats.maintenance}    />
          <StatCard label="Заблоковані"        value={stats.banned}         />
          <StatCard label="Виведені"           value={stats.decommissioned} />
        </div>

        {/* Таблиця пристроїв */}
        <div
          className="rounded-lg overflow-hidden"
          style={{ background: COLORS.bgCard, border: `1px solid ${COLORS.border}` }}
        >
          {/* Панель пошуку */}
          <div
            className="flex items-center gap-3 px-4 py-3"
            style={{ borderBottom: `1px solid ${COLORS.border}` }}
          >
            <span className="text-sm font-medium" style={{ color: COLORS.textPrimary }}>
              Пристрої
            </span>
            <div className="flex-1" />
            <input
              type="text"
              placeholder="Пошук за Hardware ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="text-sm px-3 py-1.5 rounded outline-none w-64 transition-colors"
              style={{
                border: `1px solid ${COLORS.border}`,
                background: COLORS.bgCard,
                color: COLORS.textPrimary,
              }}
              onFocus={(e) => (e.currentTarget.style.borderColor = COLORS.borderFocus)}
              onBlur={(e) => (e.currentTarget.style.borderColor = COLORS.border)}
            />
          </div>

          {/* Таблиця */}
          <table className="w-full" style={{ tableLayout: 'fixed' }}>
            <colgroup>
              <col style={{ width: '6%' }} />   {/* ID */}
              <col style={{ width: '18%' }} />  {/* Hardware ID */}
              <col style={{ width: '16%' }} />  {/* Статус */}
              <col style={{ width: '14%' }} />  {/* Формат даних */}
              <col />                            {/* Тема — залишок */}
              <col style={{ width: '5%' }} />   {/* Дії */}
            </colgroup>
            <thead>
              <tr style={{ background: COLORS.bgTableHead }}>
                {COLUMNS.map(({ label, col }) => (
                  <th
                    key={label}
                    className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
                    style={{
                      color: col && sort.col === col ? COLORS.textSecondary : COLORS.textMuted,
                      borderBottom: `1px solid ${COLORS.border}`,
                      cursor: col ? 'pointer' : 'default',
                      userSelect: 'none',
                      whiteSpace: 'nowrap',
                    }}
                    onClick={() => col && handleSort(col)}
                  >
                    {label}
                    {col && sort.col === col && (
                      <span style={{ marginLeft: 4, fontSize: 10 }}>
                        {sort.dir === 'asc' ? '▲' : '▼'}
                      </span>
                    )}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td
                    colSpan={6}
                    className="text-center py-10 text-sm"
                    style={{ color: COLORS.textMuted }}
                  >
                    Завантаження...
                  </td>
                </tr>
              ) : paged.length === 0 ? (
                <tr>
                  <td
                    colSpan={6}
                    className="text-center py-10 text-sm"
                    style={{ color: COLORS.textMuted }}
                  >
                    {search ? 'Нічого не знайдено' : 'Пристроїв немає'}
                  </td>
                </tr>
              ) : (
                paged.map((d) => (
                  <DeviceRow key={d.id} device={d} />
                ))
              )}
            </tbody>
          </table>

          {/* Пагінація */}
          {!loading && totalPages > 1 && (
            <Pagination
              page={safePage}
              total={totalPages}
              onChange={setPage}
              count={sorted.length}
            />
          )}
        </div>
      </div>
    </div>
  );
}

// Renders a single device row inside the table.

function DeviceRow({ device: d }: { device: Device }) {
  const s = STATUS_STYLES[d.status];
  return (
    <tr
      style={{ borderTop: `1px solid ${COLORS.border}` }}
      onMouseEnter={(e) => (e.currentTarget.style.background = COLORS.bgRowHover)}
      onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
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
            transition: 'background 0.15s, color 0.15s',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = COLORS.accent;
            e.currentTarget.style.color = '#fff';
            e.currentTarget.style.borderColor = COLORS.accent;
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = COLORS.bgCard;
            e.currentTarget.style.color = COLORS.textSecondary;
            e.currentTarget.style.borderColor = COLORS.border;
          }}
        >
          ›
        </Link>
      </td>
    </tr>
  );
}

// Small stat card shown at the top of the page (total, active, etc.).

function StatCard({ label, value, accent }: { label: string; value: number; accent?: string }) {
  return (
    <div
      className="rounded-lg px-4 py-3"
      style={{ background: COLORS.bgCard, border: `1px solid ${COLORS.border}` }}
    >
      <div className="text-xs uppercase tracking-wide mb-1" style={{ color: COLORS.textMuted }}>
        {label}
      </div>
      <div
        className="text-2xl font-semibold"
        style={{ color: accent ?? COLORS.textPrimary }}
      >
        {value}
      </div>
    </div>
  );
}

// Pagination bar with previous/next buttons and a window of up to 5 page numbers.

function Pagination({
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
  // Показуємо до 5 кнопок сторінок навколо поточної
  const range: number[] = [];
  const from = Math.max(1, page - 2);
  const to = Math.min(total, from + 4);
  for (let i = from; i <= to; i++) range.push(i);

  const btnBase: React.CSSProperties = {
    border: `1px solid ${COLORS.border}`,
    background: COLORS.bgCard,
    color: COLORS.textSecondary,
    borderRadius: 4,
    padding: '3px 10px',
    fontSize: 13,
    cursor: 'pointer',
    transition: 'background 0.15s',
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
    opacity: 0.4,
    cursor: 'default',
  };

  return (
    <div
      className="flex items-center justify-between px-4 py-3"
      style={{ borderTop: `1px solid ${COLORS.border}` }}
    >
      <span className="text-xs" style={{ color: COLORS.textMuted }}>
        Всього: {count}
      </span>
      <div className="flex items-center gap-1">
        <button
          style={page === 1 ? btnDisabled : btnBase}
          disabled={page === 1}
          onClick={() => onChange(page - 1)}
        >
          Назад
        </button>

        {from > 1 && (
          <>
            <button style={btnBase} onClick={() => onChange(1)}>1</button>
            {from > 2 && <span style={{ color: COLORS.textMuted, padding: '0 4px' }}>…</span>}
          </>
        )}

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

        {to < total && (
          <>
            {to < total - 1 && <span style={{ color: COLORS.textMuted, padding: '0 4px' }}>…</span>}
            <button style={btnBase} onClick={() => onChange(total)}>{total}</button>
          </>
        )}

        <button
          style={page === total ? btnDisabled : btnBase}
          disabled={page === total}
          onClick={() => onChange(page + 1)}
        >
          Вперед
        </button>
      </div>
    </div>
  );
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function _unused(_: DeviceStatus) {}
