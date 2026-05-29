/**
 * Base HTTP client. Authentication is handled via HttpOnly session cookie
 * set by the server — no credentials are stored on the client side.
 * The browser sends the cookie automatically with every request.
 */

/**
 * Wrapper around fetch that includes credentials (cookies) on every request,
 * handles 401 by redirecting to /login, and returns undefined for 204 responses.
 */
export async function apiFetch<T>(
  url: string,
  options: RequestInit = {}
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: 'include',
  });

  if (response.status === 401) {
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
