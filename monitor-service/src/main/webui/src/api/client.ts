/**
 * Base HTTP client with Basic Auth support.
 * Credentials (base64-encoded) are stored in localStorage so they persist
 * across tabs and windows. Call clearCredentials() on logout to remove them.
 */

const STORAGE_KEY = 'basic_credentials';

export const getCredentials = (): string | null =>
  localStorage.getItem(STORAGE_KEY);

export const setCredentials = (username: string, password: string): void => {
  const encoded = btoa(`${username}:${password}`);
  localStorage.setItem(STORAGE_KEY, encoded);
};

export const clearCredentials = (): void => {
  localStorage.removeItem(STORAGE_KEY);
};

/**
 * Wrapper around fetch that injects the Authorization: Basic header,
 * handles 401 by clearing credentials and redirecting to /login,
 * and returns undefined for 204 No Content responses.
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
