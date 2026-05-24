import { setCredentials, clearCredentials, getCredentials } from './client';

/**
 * Логін через ендпоінт /api/auth/verify.
 * Якщо повертає 200 — credentials валідні, зберігаємо їх у sessionStorage.
 * Якщо 401 — кидаємо помилку.
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
 * Перевірка, чи поточні збережені облікові дані ще валідні.
 * Використовується при завантаженні застосунку.
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
 * Вихід — очищуємо sessionStorage.
 */
export function logout(): void {
  clearCredentials();
}
