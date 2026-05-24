/**
 * Базовий HTTP клієнт з підтримкою Basic Auth.
 * Облікові дані (base64) зберігаються в sessionStorage —
 * автоматично видаляються при закритті вкладки.
 */

const STORAGE_KEY = 'basic_credentials';

export const getCredentials = (): string | null =>
  sessionStorage.getItem(STORAGE_KEY);

export const setCredentials = (username: string, password: string): void => {
  const encoded = btoa(`${username}:${password}`);
  sessionStorage.setItem(STORAGE_KEY, encoded);
};

export const clearCredentials = (): void => {
  sessionStorage.removeItem(STORAGE_KEY);
};

/**
 * Уніфікований fetch з автоматичним підставленням Authorization: Basic.
 */
export async function apiFetch<T>(
  url: string,
  options: RequestInit = {}
): Promise<T> {
  const credentials = getCredentials();

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };

  if (credentials) {
    headers['Authorization'] = `Basic ${credentials}`;
  }

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401) {
    clearCredentials();

    if (window.location.pathname !== '/login') {
      window.location.href = '/login';
    }
    throw new Error('Unauthorized');
  }

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}
