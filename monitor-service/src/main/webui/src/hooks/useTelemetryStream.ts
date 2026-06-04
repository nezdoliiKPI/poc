import { useEffect, useRef, useState } from 'react';
import type { TelemetryType, AnyPoint } from '../types';

const FLUSH_INTERVAL_MS  = 500;
const RETRY_DELAY_MS     = 2_000;
const MAX_RETRY_DELAY_MS = 30_000;

// ── Pure helpers ──────────────────────────────────────────────────────────────

/** Builds the SSE endpoint URL for a given device and telemetry type.
 *  Subtracts a small offset from `since` to tolerate processing latency
 *  and minor clock skew between client and server. */
function buildStreamUrl(deviceId: number, type: TelemetryType): string {
  const since = encodeURIComponent(new Date(Date.now() - 5_000).toISOString());
  return `/api/devices/${deviceId}/stream/${type}?since=${since}`;
}

/**
 * Parses a single SSE message block (the text between two blank lines).
 * Returns the parsed point, or null if the message has no valid data line.
 */
function parseSSEMessage<T>(message: string): T | null {
  const dataLine = message.split('\n').find((l) => l.startsWith('data:'));
  if (!dataLine) return null;

  const json = dataLine.slice(5).trim();
  if (!json) return null;

  try {
    return JSON.parse(json) as T;
  } catch (e) {
    console.error('SSE parse error:', e, json);
    return null;
  }
}

/**
 * Consumes a ReadableStream, splitting the byte stream into SSE message blocks
 * and calling onPoint for each successfully parsed point.
 * Stops when the stream closes or isCancelled() returns true.
 */
async function readSSEStream<T>(
  reader: ReadableStreamDefaultReader<Uint8Array>,
  decoder: TextDecoder,
  onPoint: (point: T) => void,
  isCancelled: () => boolean,
): Promise<void> {
  let buffer = '';

  while (!isCancelled()) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const messages = buffer.split('\n\n');
    buffer = messages.pop() ?? '';

    for (const message of messages) {
      const point = parseSSEMessage<T>(message);
      if (point !== null && !isCancelled()) onPoint(point);
    }
  }
}

// ── Hook ──────────────────────────────────────────────────────────────────────

/**
 * Opens an SSE stream for the given device and telemetry type, returning only live points.
 * Automatically reconnects on connection failure or stream end, using exponential backoff.
 * Incoming points are buffered in a ref and flushed into state every FLUSH_INTERVAL_MS ms.
 * Pass type = null to skip subscribing (returns empty array).
 * Points older than windowMs are dropped on each flush.
 */
export function useSSEStream<T extends AnyPoint>(
  deviceId: number,
  type: TelemetryType | null,
  windowMs: number,
): T[] {
  const [points, setPoints] = useState<T[]>([]);
  const pending = useRef<T[]>([]);

  // Clear both state and buffer when the telemetry type changes.
  // windowMs change does NOT restart the stream — the flush filter handles windowing.
  useEffect(() => {
    pending.current = [];
    setPoints([]);
  }, [type]);

  // Flush the pending buffer into state on a fixed interval.
  // windowMs IS a dependency here since it controls which accumulated points to keep.
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

  // Open the SSE connection and pipe incoming points into the pending buffer.
  // Reconnects automatically on failure or stream end (exponential backoff).
  // Does NOT depend on windowMs — the stream always delivers live data from "now",
  // and windowing is handled by the flush effect above.
  useEffect(() => {
    if (!type) return;

    let cancelled = false;
    let retryDelay = RETRY_DELAY_MS;
    const controller = new AbortController();

    const connect = async () => {
      while (!cancelled) {
        try {
          const response = await fetch(buildStreamUrl(deviceId, type), {
            headers: { Accept: 'text/event-stream' },
            credentials: 'include',
            signal: controller.signal,
          });

          if (!response.ok) {
            console.error(`SSE ${type} stream: HTTP ${response.status} for device ${deviceId}`);
            // 401 → no point retrying until the user re-authenticates
            if (response.status === 401) {
              window.location.href = '/login';
              return;
            }
          } else if (response.body) {
            retryDelay = RETRY_DELAY_MS; // reset backoff on successful connection
            await readSSEStream<T>(
              response.body.getReader(),
              new TextDecoder(),
              (point) => pending.current.push(point),
              () => cancelled,
            );
          }
        } catch (err) {
          if ((err as Error).name === 'AbortError') return;
          console.error(`SSE ${type} stream error for device ${deviceId}:`, err);
        }

        if (cancelled) return;

        // Wait before retrying, then double the delay (up to MAX_RETRY_DELAY_MS).
        await new Promise<void>((resolve) => {
          const id = setTimeout(resolve, retryDelay);
          // If cancelled during the wait, resolve immediately.
          controller.signal.addEventListener('abort', () => { clearTimeout(id); resolve(); });
        });
        retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY_MS);
      }
    };

    connect();

    return () => {
      cancelled = true;
      controller.abort();
    };
  }, [deviceId, type]); // windowMs intentionally omitted — see comment above

  return points;
}

// Kept for backward compatibility — prefer useSSEStream directly.
export const useTelemetryStream = useSSEStream;
