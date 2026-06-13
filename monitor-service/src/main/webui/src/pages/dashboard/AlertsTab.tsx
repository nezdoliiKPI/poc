import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAlertsForDevices } from '../../api/history';
import { COLORS } from '../../theme';
import { TIME_WINDOWS } from '../../theme';
import type { Device, Alert, AlertSeverity } from '../../types';
import { SEVERITY_RANK } from '../../types';
import { Pagination } from './Pagination';

const PAGE_SIZE = 12;

// Severity badge styles
const SEV_STYLES: Record<AlertSeverity, { bg: string; text: string; border: string }> = {
  WARNING:  { bg: '#fffbeb', text: '#854d0e', border: '#fde68a' },
  CRITICAL: { bg: '#fff1f1', text: '#991b1b', border: '#fecaca' },
  FAULT:    { bg: '#f5f0ff', text: '#5b21b6', border: '#ddd6fe' },
};

interface DeviceAlertRow {
  device:      Device;
  worstSev:    AlertSeverity;
  alertCount:  number;
  latestTs:    string;
}

type SortCol = 'device' | 'severity' | 'count' | 'latest';
type SortDir = 'asc' | 'desc';

export function AlertsTab({ devices = [], active = false }: { devices?: Device[]; active?: boolean }) {
  const navigate = useNavigate();

  const [rows,          setRows]          = useState<DeviceAlertRow[]>([]);
  const [loading,       setLoading]       = useState(false);
  const [windowMinutes, setWindowMinutes] = useState(60);
  const [sort,          setSort]          = useState<{ col: SortCol; dir: SortDir }>({ col: 'latest', dir: 'desc' });
  const [page,          setPage]          = useState(1);

  // Fetch alerts only when the tab is visible — runs immediately on open, then every 5 seconds.
  // An in-flight guard prevents concurrent requests if one takes longer than the interval.
  useEffect(() => {
    if (!active || devices.length === 0) return;

    const deviceMap = new Map(devices.map((d) => [d.id, d]));
    let inFlight = false;
    let cancelled = false;

    const fetchAlerts = async () => {
      if (inFlight || cancelled) return;
      inFlight = true;
      setLoading(true);

      const to   = new Date();
      const from = new Date(to.getTime() - windowMinutes * 60 * 1000);

      try {
        const alerts = await getAlertsForDevices(devices.map((d) => d.id), from, to);
        if (cancelled) return;

        // Group alerts by deviceId.
        const byDevice = new Map<number, Alert[]>();
        alerts.forEach((a) => {
          const list = byDevice.get(a.dID) ?? [];
          list.push(a);
          byDevice.set(a.dID, list);
        });

        const built: DeviceAlertRow[] = [];
        byDevice.forEach((devAlerts, dID) => {
          const device = deviceMap.get(dID);
          if (!device) return;
          const worstSev = devAlerts.reduce<AlertSeverity>((best, a) =>
            SEVERITY_RANK[a.sev] > SEVERITY_RANK[best] ? a.sev : best, 'WARNING',
          );
          const latestTs = devAlerts.reduce(
            (latest, a) => (a.ts > latest ? a.ts : latest),
            devAlerts[0].ts,
          );
          built.push({ device, worstSev, alertCount: devAlerts.length, latestTs });
        });
        if (!cancelled) setRows(built);
      } finally {
        inFlight = false;
        if (!cancelled) setLoading(false);
      }
    };

    fetchAlerts();
    const timerId = setInterval(fetchAlerts, 5_000);
    return () => { cancelled = true; clearInterval(timerId); };
  }, [active, devices, windowMinutes]);

  useEffect(() => { setPage(1); }, [sort, windowMinutes]);

  const handleSort = (col: SortCol) =>
    setSort((prev) => prev.col === col
      ? { col, dir: prev.dir === 'asc' ? 'desc' : 'asc' }
      : { col, dir: col === 'latest' ? 'desc' : 'asc' });

  const sorted = useMemo(() => [...rows].sort((a, b) => {
    let cmp = 0;
    if (sort.col === 'device')   cmp = a.device.hardwareId.localeCompare(b.device.hardwareId, 'uk');
    if (sort.col === 'severity') cmp = SEVERITY_RANK[a.worstSev] - SEVERITY_RANK[b.worstSev];
    if (sort.col === 'count')    cmp = a.alertCount - b.alertCount;
    if (sort.col === 'latest')   cmp = a.latestTs.localeCompare(b.latestTs);
    return sort.dir === 'asc' ? cmp : -cmp;
  }), [rows, sort]);

  const totalPages = Math.max(1, Math.ceil(sorted.length / PAGE_SIZE));
  const safePage   = Math.min(page, totalPages);
  const paged      = sorted.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const colStyle = (col: SortCol): React.CSSProperties => ({
    color: sort.col === col ? COLORS.textSecondary : COLORS.textMuted,
    cursor: 'pointer',
    userSelect: 'none',
    whiteSpace: 'nowrap',
  });

  const arrow = (col: SortCol) => sort.col === col
    ? <span style={{ marginLeft: 4, fontSize: 10 }}>{sort.dir === 'asc' ? '▲' : '▼'}</span>
    : null;

  return (
    <div>
      {/* Time window selector */}
      <div className="flex items-center gap-2" style={{ marginBottom: 16 }}>
        <label className="text-xs uppercase tracking-wide" style={{ color: COLORS.textMuted }}>
          Період:
        </label>
        <select
          value={windowMinutes}
          onChange={(e) => setWindowMinutes(Number(e.target.value))}
          style={{
            padding: '4px 10px', borderRadius: 6, fontSize: 13,
            border: `1px solid ${COLORS.border}`, background: COLORS.bgCard,
            color: COLORS.textPrimary, cursor: 'pointer', outline: 'none',
          }}
        >
          {TIME_WINDOWS.map(({ label, minutes }) => (
            <option key={minutes} value={minutes}>{label}</option>
          ))}
        </select>
        {!loading && (
          <span style={{ fontSize: 12, color: COLORS.textMuted, marginLeft: 8 }}>
            {rows.length === 0
              ? 'Сповіщень немає'
              : `${rows.length} пристро${rows.length === 1 ? 'їв' : 'їв'} з сповіщеннями`}
          </span>
        )}
      </div>

      {/* Table */}
      <div style={{ border: `1px solid ${COLORS.border}`, borderRadius: 6, overflow: 'hidden' }}>
        <table className="w-full" style={{ tableLayout: 'fixed' }}>
          <colgroup>
            <col style={{ width: '6%' }} />
            <col style={{ width: '28%' }} />
            <col style={{ width: '16%' }} />
            <col style={{ width: '12%' }} />
            <col />
            <col style={{ width: '5%' }} />
          </colgroup>
          <thead>
            <tr style={{ background: COLORS.bgTableHead }}>
              <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
                style={{ color: COLORS.textMuted, borderBottom: `1px solid ${COLORS.border}` }}>
                ID
              </th>
              <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
                style={{ ...colStyle('device'), borderBottom: `1px solid ${COLORS.border}` }}
                onClick={() => handleSort('device')}>
                Hardware ID {arrow('device')}
              </th>
              <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
                style={{ ...colStyle('severity'), borderBottom: `1px solid ${COLORS.border}` }}
                onClick={() => handleSort('severity')}>
                Ступінь {arrow('severity')}
              </th>
              <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
                style={{ ...colStyle('count'), borderBottom: `1px solid ${COLORS.border}` }}
                onClick={() => handleSort('count')}>
                Кількість {arrow('count')}
              </th>
              <th className="px-4 py-2.5 text-left text-xs font-semibold uppercase tracking-wide"
                style={{ ...colStyle('latest'), borderBottom: `1px solid ${COLORS.border}` }}
                onClick={() => handleSort('latest')}>
                Останнє {arrow('latest')}
              </th>
              <th className="px-4 py-2.5" style={{ borderBottom: `1px solid ${COLORS.border}` }} />
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
                  Сповіщень за вибраний період немає
                </td>
              </tr>
            ) : (
              paged.map(({ device, worstSev, alertCount, latestTs }) => {
                const s = SEV_STYLES[worstSev];
                const ts = new Date(latestTs);
                const fmtTs = `${ts.toLocaleDateString('uk-UA')} ${ts.toLocaleTimeString('uk-UA', { hour: '2-digit', minute: '2-digit' })}`;
                return (
                  <tr
                    key={device.id}
                    className="hover-row"
                    style={{ borderTop: `1px solid ${COLORS.border}` }}
                  >
                    <td className="px-4 py-3 text-sm" style={{ color: COLORS.textSecondary }}>
                      {device.id}
                    </td>
                    <td className="px-4 py-3 font-mono text-sm" style={{ color: COLORS.textPrimary }}>
                      {device.hardwareId}
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-xs font-medium px-2 py-0.5 rounded"
                        style={{ background: s.bg, color: s.text, border: `1px solid ${s.border}` }}>
                        {worstSev}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm" style={{ color: COLORS.textSecondary }}>
                      {alertCount}
                    </td>
                    <td className="px-4 py-3 text-sm font-mono" style={{ color: COLORS.textMuted, fontSize: 12 }}>
                      {fmtTs}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <button
                        className="hover-primary"
                        onClick={() => navigate(`/devices/${device.id}#alerts`, { state: { windowMinutes } })}
                        style={{
                          display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                          width: 26, height: 26, borderRadius: 4, fontSize: 16, lineHeight: 1,
                          border: `1px solid ${COLORS.border}`, color: COLORS.textSecondary,
                          background: COLORS.bgCard, cursor: 'pointer',
                        }}
                      >
                        ›
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>

        {!loading && totalPages > 1 && (
          <Pagination page={safePage} total={totalPages} onChange={setPage} count={sorted.length} />
        )}
      </div>
    </div>
  );
}
