import { useEffect, useRef, useState } from 'react';
import type { Alert } from '../types';

const FLUSH_INTERVAL_MS  = 500;
const RETRY_DELAY_MS     = 2_000;
const MAX_RETRY_DELAY_MS = 30_000;

/**
 * Opens an SSE stream for alert events for a specific device.
 * Automatically reconnects on failure or stream end (exponential backoff).
 * Buffers incoming alerts and flushes them into state every FLUSH_INTERVAL_MS ms.
 * Points older than windowMs are dropped on each flush.
 */
export function useAlertStream(deviceId: number, windowMs: number): Alert[] {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const pending = useRef<Alert[]>([]);

  // Clear on deviceId change.
  useEffect(() => {
    pending.current = [];
    setAlerts([]);
  }, [deviceId]);

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

  // Open the SSE connection with automatic reconnect on failure or stream end.
  useEffect(() => {
    let cancelled = false;
    let retryDelay = RETRY_DELAY_MS;
    const controller = new AbortController();

    const connect = async () => {
      while (!cancelled) {
        // Subtract a small offset from `since` to tolerate processing latency / clock skew.
        const since = encodeURIComponent(new Date(Date.now() - 5_000).toISOString());

        try {
          const response = await fetch(
            `/api/devices/${deviceId}/stream/alert?since=${since}`,
            {
              headers: { Accept: 'text/event-stream' },
              credentials: 'include',
              signal: controller.signal,
            }
          );

          if (!response.ok) {
            console.error(`Alert SSE stream: HTTP ${response.status} for device ${deviceId}`);
            if (response.status === 401) {
              window.location.href = '/login';
              return;
            }
          } else if (response.body) {
            retryDelay = RETRY_DELAY_MS; // reset backoff on successful connection
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
                  console.error('Alert SSE parse error:', e, json);
                }
              }
            }
          }
        } catch (err) {
          if ((err as Error).name === 'AbortError') return;
          console.error(`Alert SSE stream error for device ${deviceId}:`, err);
        }

        if (cancelled) return;

        await new Promise<void>((resolve) => {
          const id = setTimeout(resolve, retryDelay);
          controller.signal.addEventListener('abort', () => { clearTimeout(id); resolve(); });
        });
        retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY_MS);
      }
    };

    connect();

    return () => { cancelled = true; controller.abort(); };
  }, [deviceId]);

  return alerts;
}
