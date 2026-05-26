import { useEffect, useState } from 'react';
import { getCredentials } from '../api/client';
import type { TelemetryType, AnyPoint } from '../types';

/**
 * Opens an SSE stream for the given device and telemetry type, returning only live points.
 * Historical data is not fetched here — the component merges history with these live points.
 * Pass type = null to skip subscribing (returns empty array).
 * Points older than windowMs are dropped as new data arrives.
 */
export function useSSEStream<T extends AnyPoint>(
  deviceId: number,
  type: TelemetryType | null,
  windowMs: number
): T[] {
  const [points, setPoints] = useState<T[]>([]);

  // Clear accumulated points when the type or window changes so stale data doesn't carry over.
  useEffect(() => {
    setPoints([]);
  }, [type, windowMs]);

  useEffect(() => {
    if (!type) return;

    const credentials = getCredentials();
    if (!credentials) return;

    let cancelled = false;
    const controller = new AbortController();

    (async () => {
      try {
        const response = await fetch(
          `/api/devices/${deviceId}/stream/${type}`,
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
              if (cancelled) return;

              setPoints((prev) => {
                const cutoff = Date.now() - windowMs;
                return [
                  ...prev.filter((p) => new Date(p.timeDate).getTime() > cutoff),
                  point,
                ];
              });
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
