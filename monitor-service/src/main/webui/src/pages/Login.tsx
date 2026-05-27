import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { COLORS } from '../theme';

/**
 * Login page — styled to match the Producer Panel reference design.
 * Blue accent bar at the top of the card, labeled inputs, uppercase submit button.
 */
export default function Login() {
  const { login }   = useAuth();
  const navigate    = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error,    setError]    = useState('');
  const [loading,  setLoading]  = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 16,
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: 380,
          background: '#ffffff',
          borderRadius: 10,
          boxShadow: '0 4px 24px rgba(0,0,0,0.10)',
          overflow: 'hidden',
        }}
      >
        {/* Blue accent bar */}
        <div
          style={{
            height: 5,
            background: 'linear-gradient(90deg, #1d4ed8 0%, #38bdf8 100%)',
          }}
        />

        {/* Card body */}
        <div style={{ padding: '36px 36px 32px' }}>
          {/* Heading */}
          <div style={{ textAlign: 'center', marginBottom: 32 }}>
            <h1
              style={{
                margin: 0,
                fontSize: 24,
                fontWeight: 700,
                color: '#1c2333',
                letterSpacing: '-0.03em',
              }}
            >
              Панель адміністратора
            </h1>
            <p
              style={{
                margin: '6px 0 0',
                fontSize: 12,
                fontFamily: 'monospace',
                color: '#8c97ad',
                letterSpacing: '0.02em',
              }}
            >
              Вхід
            </p>
          </div>

          <form onSubmit={handleSubmit}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
              {/* Username */}
              <div>
                <label
                  htmlFor="login-username"
                  style={{
                    display: 'block',
                    fontSize: 11,
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    letterSpacing: '0.08em',
                    color: '#5a6680',
                    marginBottom: 6,
                  }}
                >
                  Ім'я користувача
                </label>
                <input
                  id="login-username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  autoComplete="username"
                  style={{
                    width: '100%',
                    padding: '10px 14px',
                    borderRadius: 6,
                    border: '1.5px solid #cdd1d9',
                    fontSize: 14,
                    color: '#1c2333',
                    outline: 'none',
                    boxSizing: 'border-box',
                    transition: 'border-color 0.15s',
                  }}
                  onFocus={(e) => (e.currentTarget.style.borderColor = COLORS.borderFocus)}
                  onBlur={(e)  => (e.currentTarget.style.borderColor = '#cdd1d9')}
                />
              </div>

              {/* Password */}
              <div>
                <label
                  htmlFor="login-password"
                  style={{
                    display: 'block',
                    fontSize: 11,
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    letterSpacing: '0.08em',
                    color: '#5a6680',
                    marginBottom: 6,
                  }}
                >
                  Пароль
                </label>
                <input
                  id="login-password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="current-password"
                  style={{
                    width: '100%',
                    padding: '10px 14px',
                    borderRadius: 6,
                    border: '1.5px solid #cdd1d9',
                    fontSize: 14,
                    color: '#1c2333',
                    outline: 'none',
                    boxSizing: 'border-box',
                    transition: 'border-color 0.15s',
                  }}
                  onFocus={(e) => (e.currentTarget.style.borderColor = COLORS.borderFocus)}
                  onBlur={(e)  => (e.currentTarget.style.borderColor = '#cdd1d9')}
                />
              </div>

              {/* Error message */}
              {error && (
                <div
                  style={{
                    fontSize: 13,
                    padding: '9px 13px',
                    borderRadius: 6,
                    background: COLORS.dangerBg,
                    border: `1px solid ${COLORS.dangerBorder}`,
                    color: COLORS.dangerText,
                  }}
                >
                  {error}
                </div>
              )}

              {/* Submit button */}
              <button
                type="submit"
                disabled={loading}
                className="hover-accent-solid"
                style={{
                  marginTop: 4,
                  width: '100%',
                  padding: '12px',
                  borderRadius: 6,
                  border: 'none',
                  background: loading ? '#93b4f3' : COLORS.accent,
                  color: '#ffffff',
                  fontSize: 13,
                  fontWeight: 700,
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  cursor: loading ? 'not-allowed' : 'pointer',
                }}
              >
                {loading ? 'Перевірка...' : 'УВІЙТИ'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
