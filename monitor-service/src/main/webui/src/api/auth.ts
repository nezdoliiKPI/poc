/**
 * Auth API — login, logout, and session check.
 * Credentials are never stored on the client; the server manages the session
 * via an HttpOnly cookie.
 */

/**
 * Sends username/password to the server. On success the server sets an
 * HttpOnly session cookie; subsequent requests carry it automatically.
 */
export async function login(username: string, password: string): Promise<void> {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ username, password }),
  });

  if (response.status === 401) {
    throw new Error('Невірний логін або пароль');
  }
  if (!response.ok) {
    throw new Error(`Помилка сервера: ${response.status}`);
  }
}

/**
 * Checks whether the current session cookie is still valid.
 * Returns true if the server recognises the session, false otherwise.
 */
export async function verifyAuth(): Promise<boolean> {
  try {
    const response = await fetch('/api/auth/me', { credentials: 'include' });
    return response.ok;
  } catch {
    return false;
  }
}

/**
 * Invalidates the session on the server and clears the cookie.
 */
export async function logout(): Promise<void> {
  await fetch('/api/auth/logout', {
    method: 'POST',
    credentials: 'include',
  });
}
