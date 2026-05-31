

export interface ProducerConfig {
  powerProtoCount: number;
  powerJsonCount:  number;
  smokeProtoCount: number;
  smokeJsonCount:  number;
  airProtoCount:   number;
  airJsonCount:    number;
  tempProtoCount:  number;
  tempJsonCount:   number;
  intensity:       number | null;
}

export interface EdgeConfig {
  topic:     string;
  consume:   boolean;
  threshold: number;
}

export type ThresholdType = 'power' | 'air' | 'battery' | 'smoke' | 'temperature';

/**
 * Parses Quarkus/RESTEasy error response bodies into a readable message string.
 */
function parseErrorBody(text: string, status: number): string {
  try {
    const err = JSON.parse(text);
    if (err.violations?.length) {
      return (err.violations as { message: string }[]).map((v) => v.message).join('; ');
    }
    return err.title ?? err.message ?? `Server error (${status})`;
  } catch {
    return text || `Server error (${status})`;
  }
}

/**
 * Makes an authenticated POST request to an admin endpoint.
 * Redirects to /login on 401. Throws on non-2xx responses.
 * Tries to parse JSON body if content-type is application/json; otherwise returns undefined.
 * Throws if the response is HTML -- this happens when the proxy points to the wrong service
 * and Quinoa's SPA fallback serves index.html instead of the actual API response.
 */
async function adminGet<T>(url: string): Promise<T> {
  const resp = await fetch(url, {
    headers: { Accept: 'application/json' },
    credentials: 'include',
  });
  if (resp.status === 401) { window.location.href = '/login'; throw new Error('Unauthorized'); }
  if (!resp.ok) { const text = await resp.text(); throw new Error(parseErrorBody(text, resp.status)); }
  const contentType = resp.headers.get('content-type') ?? '';
  if (contentType.includes('text/html')) {
    throw new Error(
      'Endpoint ' + url + ' returned HTML instead of an API response. ' +
      'Check the Vite proxy settings and the configuration service address.'
    );
  }
  return resp.json();
}

async function adminPost<T = void>(url: string, body: unknown): Promise<T> {
  const resp = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body),
  });

  if (resp.status === 401) {
    window.location.href = '/login';
    throw new Error('Unauthorized');
  }

  if (!resp.ok) {
    const text = await resp.text();
    throw new Error(parseErrorBody(text, resp.status));
  }

  const contentType = resp.headers.get('content-type') ?? '';

  // Guard against the proxy returning the SPA's index.html instead of a real API response.
  // This happens when Vite forwards the request to a service that doesn't have the endpoint
  // and Quinoa's SPA routing intercepts it and returns 200 text/html.
  if (contentType.includes('text/html')) {
    throw new Error(
      'Endpoint ' + url + ' returned HTML instead of an API response. ' +
      'Check the Vite proxy settings and the configuration service address.'
    );
  }

  if (contentType.includes('application/json')) {
    return (await resp.json()) as T;
  }

  // 202 Accepted / 200 with no body -- success with no payload.
  return undefined as T;
}

/**
 * Fetches the current producer (data generator) configuration.
 */
export function getProducerConfig(): Promise<ProducerConfig> {
  return adminGet<ProducerConfig>('/api/producer/gen');
}

/**
 * Sends updated producer config to the server.
 * Returns the resulting ProducerConfig as confirmed by the server.
 */
export function updateProducerConfig(config: ProducerConfig): Promise<ProducerConfig> {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { intensity: _intensity, ...body } = config;
  return adminPost<ProducerConfig>('/api/producer/gen/update', body);
}

/**
 * Sends a single edge filter config entry to /api/edge/update.
 */
export function updateEdgeConfig(config: EdgeConfig): Promise<void> {
  return adminPost('/api/edge/update', config);
}

/**
 * Sends threshold config to /api/thresholds/{type}.
 */
export function updateThresholds(type: ThresholdType, payloads: object[]): Promise<void> {
  return adminPost('/api/thresholds/' + type, payloads);
}
