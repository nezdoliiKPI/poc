import { useEffect, useRef, useState } from 'react';
import type { TelemetryType, AnyPoint } from '../types';

const FLUSH_INTERVAL_MS = 500;

// ── Pure helpers ──────────────────────────────────────────────────────────────

/** Builds the SSE endpoint URL for a given device and telemetry type. */
function buildStreamUrl(deviceId: number, type: TelemetryType): string {
  const since = encodeURIComponent(new Date().toISOString());
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
    console.error('SSE parse error:', e);
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
 * Incoming points are buffered in a ref and flushed into state every FLUSH_INTERVAL_MS ms
 * to avoid a re-render on every individual message when data arrives at high frequency.
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

  // Open the SSE connection and pipe incoming points into the pending buffer.
  useEffect(() => {
    if (!type) return;

    let cancelled = false;
    const controller = new AbortController();

    (async () => {
      try {
        const response = await fetch(buildStreamUrl(deviceId, type), {
          headers: { Accept: 'text/event-stream' },
          credentials: 'include',
          signal: controller.signal,
        });

        if (!response.ok || !response.body) return;

        await readSSEStream<T>(
          response.body.getReader(),
          new TextDecoder(),
          (point) => pending.current.push(point),
          () => cancelled,
        );
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
