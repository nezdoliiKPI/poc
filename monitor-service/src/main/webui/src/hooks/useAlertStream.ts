import { useEffect, useRef, useState } from 'react';
import type { Alert } from '../types';

const FLUSH_INTERVAL_MS = 500;

/**
 * Opens an SSE stream for alert events for a specific device.
 * Buffers incoming alerts and flushes them into state every FLUSH_INTERVAL_MS ms.
 * Points older than windowMs are dropped on each flush.
 */
export function useAlertStream(deviceId: number, windowMs: number): Alert[] {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const pending = useRef<Alert[]>([]);

  // Clear on window change.
  useEffect(() => {
    pending.current = [];
    setAlerts([]);
  }, [windowMs]);

  // Flush buffer into state on a fixed interval.
  useEffect(() => {
    const timerId = setInterval(() => {
      if (pending.current.length === 0) return;
      const batch = pending.current.splice(0);
      setAlerts((prev) => {
        const cutoff = Date.now() - windowMs;
        return [
          ...prev.filter((a) => new Date(a.ts).getTime() > cutoff),
          ...batch,
        ];
      });
    }, FLUSH_INTERVAL_MS);
    return () => clearInterval(timerId);
  }, [windowMs]);

  // Open the SSE connection.
  useEffect(() => {
    let cancelled = false;
    const controller = new AbortController();
    const since = encodeURIComponent(new Date().toISOString());

    (async () => {
      try {
        const response = await fetch(
          `/api/devices/${deviceId}/stream/alert?since=${since}`,
          {
            headers: { Accept: 'text/event-stream' },
            credentials: 'include',
            signal: controller.signal,
          }
        );

        if (!response.ok || !response.body) return;

        const reader  = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer    = '';

        while (!cancelled) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const messages = buffer.split('\n\n');
          buffer = messages.pop() ?? '';

          for (const message of messages) {
            const dataLine = message.split('\n').find((l) => l.startsWith('data:'));
            if (!dataLine) continue;
            const json = dataLine.slice(5).trim();
            if (!json) continue;
            try {
              const alert: Alert = JSON.parse(json);
              if (!cancelled) pending.current.push(alert);
            } catch (e) {
              console.error('Alert SSE parse error:', e);
            }
          }
        }
      } catch (err) {
        if (!cancelled && (err as Error).name !== 'AbortError') {
          console.error('Alert SSE stream error:', err);
        }
      }
    })();

    return () => { cancelled = true; controller.abort(); };
  }, [deviceId, windowMs]);

  return alerts;
}
