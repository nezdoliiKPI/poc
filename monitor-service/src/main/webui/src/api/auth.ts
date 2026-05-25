import { setCredentials, clearCredentials, getCredentials } from './client';

/**
 * Authenticates against /api/auth/verify using Basic Auth.
 * On 200 the credentials are persisted to sessionStorage.
 * On 401 an error is thrown with a user-facing message.
 */
export async function login(
  username: string,
  password: string
): Promise<void> {
  const encoded = btoa(`${username}:${password}`);

  const response = await fetch('/api/auth/verify', {
    headers: { Authorization: `Basic ${encoded}` },
  });

  if (response.status === 401) {
    throw new Error('Невірний логін або пароль');
  }

  if (!response.ok) {
    throw new Error(`Помилка сервера: ${response.status}`);
  }

  setCredentials(username, password);
}

/**
 * Checks whether the stored credentials are still valid.
 * Called on application startup to decide whether to show the login screen.
 */
export async function verifyAuth(): Promise<boolean> {
  const credentials = getCredentials();
  if (!credentials) return false;

  try {
    const response = await fetch('/api/auth/verify', {
      headers: { Authorization: `Basic ${credentials}` },
    });
    return response.ok;
  } catch {
    return false;
  }
}

/**
 * Clears stored credentials and effectively logs the user out.
 */
export function logout(): void {
  clearCredentials();
}
