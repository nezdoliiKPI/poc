import { useEffect, useRef, useState } from 'react';
import { getCredentials } from '../api/client';
import type { TelemetryType, AnyPoint } from '../types';

const FLUSH_INTERVAL_MS = 500;

/**
 * Opens an SSE stream for the given device and telemetry type, returning only live points.
 * Incoming points are buffered in a ref and flushed into state every FLUSH_INTERVAL_MS ms
 * to avoid a re-render on every individual message when data arrives at high frequency.
 * Pass type = null to skip subscribing (returns empty array).
 * Points older than windowMs are dropped on each flush.
 */
export function useSSEStream<T extends AnyPoint>(
  deviceId: number,
  type: TelemetryType | null,
  windowMs: number
): T[] {
  const [points, setPoints] = useState<T[]>([]);
  const pending = useRef<T[]>([]);

  // Clear both state and buffer when the type or window changes.
  useEffect(() => {
    pending.current = [];
    setPoints([]);
  }, [type, windowMs]);

  // Flush the pending buffer into state on a fixed interval.
  useEffect(() => {
    if (!type) return;

    const timerId = setInterval(() => {
      if (pending.current.length === 0) return;
      const batch = pending.current.splice(0);
      setPoints((prev) => {
        const cutoff = Date.now() - windowMs;
        return [
          ...prev.filter((p) => new Date(p.timeDate).getTime() > cutoff),
          ...batch,
        ];
      });
    }, FLUSH_INTERVAL_MS);

    return () => clearInterval(timerId);
  }, [type, windowMs]);

  useEffect(() => {
    if (!type) return;

    const credentials = getCredentials();
    if (!credentials) return;

    let cancelled = false;
    const controller = new AbortController();

    (async () => {
      try {
        const since = new Date().toISOString();
        const response = await fetch(
          `/api/devices/${deviceId}/stream/${type}?since=${encodeURIComponent(since)}`,
          {
            headers: {
              Authorization: `Basic ${credentials}`,
              Accept: 'text/event-stream',
            },
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
            const dataLine = message
              .split('\n')
              .find((l) => l.startsWith('data:'));
            if (!dataLine) continue;

            const json = dataLine.slice(5).trim();
            if (!json) continue;

            try {
              const point: T = JSON.parse(json);
              if (!cancelled) pending.current.push(point);
            } catch (e) {
              console.error('SSE parse error:', e);
            }
          }
        }
      } catch (err) {
        if (!cancelled && (err as Error).name !== 'AbortError') {
          console.error('SSE stream error:', err);
        }
      }
    })();

    return () => {
      cancelled = true;
      controller.abort();
    };
  }, [deviceId, type, windowMs]);

  return points;
}

// Kept for backward compatibility — prefer useSSEStream directly.
export const useTelemetryStream = useSSEStream;
