import { useEffect, useState, type CSSProperties } from 'react';
import { getDevices } from '../../api/devices';
import { useAuth } from '../../hooks/useAuth';
import { useError } from '../../hooks/useError';
import { COLORS } from '../../theme';
import type { Device, DeviceStatus } from '../../types';
import { GenTab, FilterTab, ThresholdsTab } from '../admin';
import { DeviceRow } from './DeviceRow';
import { Pagination } from './Pagination';

const PAGE_SIZE = 10;

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

// Shared card style used by all four tab content areas.
const TAB_CARD: CSSProperties = {
  background: COLORS.bgCard,
  border: `1px solid ${COLORS.border}`,
  borderRadius: 8,
  marginTop: 20,
  marginLeft: 'auto',
  marginRight: 'auto',
  maxWidth: 1024,
  padding: '24px 28px',
};

/**
 * Minimal stat cell shown in the summary row above the device table.
 */
function StatCell({ label, value }: { label: string; value: number }) {
  return (
    <div style={{ textAlign: 'center' }}>
      <div style={{ fontSize: 22, fontWeight: 600, color: COLORS.textPrimary, lineHeight: 1.1 }}>
        {value}
      </div>
      <div style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.05em', color: COLORS.textMuted, marginTop: 3 }}>
        {label}
      </div>
    </div>
  );
}

/**
 * Main dashboard page — device list with sorting, search, pagination, and admin tabs.
 */
export default function Dashboard() {
  const { logout } = useAuth();
  const { showError } = useError();

  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [search,  setSearch]  = useState('');
  const [page,    setPage]    = useState(1);
  const [sort,    setSort]    = useState<{ col: SortCol; dir: SortDir }>({ col: 'id', dir: 'asc' });
  const [mainTab, setMainTab] = useState<'devices' | 'gen' | 'filter' | 'thresholds'>('devices');

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
    String(d.id).includes(search.trim()) || d.hardwareId.toLowerCase().includes(search.toLowerCase())
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
  const safePage   = Math.min(page, totalPages);
  const paged      = sorted.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const stats = {
    total:          devices.length,
    active:         devices.filter((d) => d.status === 'ACTIVE').length,
    maintenance:    devices.filter((d) => d.status === 'MAINTENANCE').length,
    banned:         devices.filter((d) => d.status === 'BANNED').length,
    decommissioned: devices.filter((d) => d.status === 'DECOMMISSIONED').length,
  };

  return (
    <div className="min-h-screen" style={{ background: COLORS.bgPage }}>

      {/* ── Header + tabs merged into one bar ──────────────────────────── */}
      <header
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr auto 1fr',
          alignItems: 'stretch',
          background: COLORS.bgCard,
          borderBottom: `1px solid ${COLORS.border}`,
          paddingLeft: 24,
          paddingRight: 24,
        }}
      >
        {/* Left: brand title */}
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <span style={{ fontSize: 14, fontWeight: 600, color: COLORS.textPrimary }}>
            Моніторинг пристроїв
          </span>
        </div>

        {/* Center: navigation tabs */}
        <div style={{ display: 'flex', alignItems: 'stretch' }}>
          {(
            [
              { id: 'devices',    label: 'Пристрої'  },
              { id: 'gen',        label: 'Генерація' },
              { id: 'filter',     label: 'Фільтри'   },
              { id: 'thresholds', label: 'Пороги'    },
            ] as const
          ).map(({ id, label }) => (
            <button
              key={id}
              onClick={() => setMainTab(id)}
              style={{
                background: 'transparent',
                border: 'none',
                borderBottom: mainTab === id
                  ? `2px solid ${COLORS.accent}`
                  : '2px solid transparent',
                color: mainTab === id ? COLORS.accent : COLORS.textMuted,
                fontSize: 13,
                fontWeight: mainTab === id ? 600 : 400,
                padding: '0 18px',
                height: 48,
                cursor: 'pointer',
                transition: 'color 0.15s',
                marginBottom: -1,
                whiteSpace: 'nowrap',
              }}
            >
              {label}
            </button>
          ))}
        </div>

        {/* Right: logout button */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
          <button
            onClick={logout}
            className="hover-danger"
            style={{
              fontSize: 12,
              padding: '4px 12px',
              borderRadius: 5,
              border: `1px solid ${COLORS.border}`,
              color: COLORS.textSecondary,
              background: 'transparent',
              cursor: 'pointer',
            }}
          >
            Вийти
          </button>
        </div>
      </header>

      {/* ── Devices tab ─────────────────────────────────────────────────── */}
      <div style={{ ...TAB_CARD, display: mainTab === 'devices' ? undefined : 'none' }}>

        {/* Stats row — minimal text-only summary */}
        <div
          style={{
            display: 'flex',
            gap: 0,
            marginBottom: 20,
            borderBottom: `1px solid ${COLORS.border}`,
            paddingBottom: 18,
          }}
        >
          {[
            { label: 'Всього',            value: stats.total          },
            { label: 'Активні',           value: stats.active         },
            { label: 'На обслуговуванні', value: stats.maintenance    },
            { label: 'Заблоковані',       value: stats.banned         },
            { label: 'Виведені',          value: stats.decommissioned },
          ].map(({ label, value }, i) => (
            <div
              key={label}
              style={{
                flex: 1,
                textAlign: 'center',
                borderLeft: i > 0 ? `1px solid ${COLORS.border}` : 'none',
              }}
            >
              <StatCell label={label} value={value} />
            </div>
          ))}
        </div>

        {/* Search toolbar */}
        <div className="flex items-center gap-3" style={{ marginBottom: 12 }}>
          <span style={{ fontSize: 13, fontWeight: 500, color: COLORS.textPrimary }}>
            Список пристроїв
          </span>
          <div style={{ flex: 1 }} />
          <input
            type="text"
            placeholder="Пошук за ID або Hardware ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{
              fontSize: 13,
              padding: '6px 12px',
              borderRadius: 5,
              border: `1px solid ${COLORS.border}`,
              background: COLORS.bgTableHead,
              color: COLORS.textPrimary,
              outline: 'none',
              width: 240,
              transition: 'border-color 0.15s',
            }}
            onFocus={(e) => (e.currentTarget.style.borderColor = COLORS.borderFocus)}
            onBlur={(e)  => (e.currentTarget.style.borderColor = COLORS.border)}
          />
        </div>

        {/* Table */}
        <div
          style={{
            border: `1px solid ${COLORS.border}`,
            borderRadius: 6,
            overflow: 'hidden',
          }}
        >
          <table className="w-full" style={{ tableLayout: 'fixed' }}>
            <colgroup>
              <col style={{ width: '6%' }} />
              <col style={{ width: '18%' }} />
              <col style={{ width: '16%' }} />
              <col style={{ width: '14%' }} />
              <col />
              <col style={{ width: '5%' }} />
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
                  <td colSpan={6} className="text-center py-10 text-sm" style={{ color: COLORS.textMuted }}>
                    Завантаження...
                  </td>
                </tr>
              ) : paged.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-10 text-sm" style={{ color: COLORS.textMuted }}>
                    {search ? 'Нічого не знайдено' : 'Пристроїв немає'}
                  </td>
                </tr>
              ) : (
                paged.map((d) => <DeviceRow key={d.id} device={d} />)
              )}
            </tbody>
          </table>

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

      {/* ── Admin tabs — kept mounted so state survives tab switches ─────── */}
      {(['gen', 'filter', 'thresholds'] as const).map((id) => (
        <div key={id} style={{ ...TAB_CARD, display: mainTab === id ? undefined : 'none' }}>
          {id === 'gen'        && <GenTab />}
          {id === 'filter'     && <FilterTab />}
          {id === 'thresholds' && <ThresholdsTab />}
        </div>
      ))}

    </div>
  );
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function _unused(_: DeviceStatus) {}
