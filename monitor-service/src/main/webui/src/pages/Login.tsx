import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { COLORS } from '../theme';

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: '10px 12px',
  borderRadius: 6,
  border: `1px solid ${COLORS.border}`,
  background: COLORS.bgCard,
  color: COLORS.textPrimary,
  fontSize: 14,
  outline: 'none',
  transition: 'border-color 0.15s',
  boxSizing: 'border-box',
};

export default function Login() {
  const { login } = useAuth();
  const navigate  = useNavigate();

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

  const focus = (e: React.FocusEvent<HTMLInputElement>) =>
    (e.currentTarget.style.borderColor = COLORS.borderFocus);
  const blur = (e: React.FocusEvent<HTMLInputElement>) =>
    (e.currentTarget.style.borderColor = COLORS.border);

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: COLORS.bgPage,
        padding: '16px',
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: 360,
          background: COLORS.bgCard,
          border: `1px solid ${COLORS.border}`,
          borderRadius: 10,
          padding: '36px 32px',
          boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
        }}
      >
        {/* Заголовок */}
        <div style={{ marginBottom: 28, textAlign: 'center' }}>
          <h1
            style={{
              margin: 0,
              fontSize: 22,
              fontWeight: 700,
              color: COLORS.textPrimary,
              letterSpacing: '-0.03em',
            }}
          >
            Вхід
          </h1>
          <p
            style={{
              margin: '4px 0 0',
              fontSize: 13,
              color: COLORS.textMuted,
              fontWeight: 400,
            }}
          >
            Панель адміністратора
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <input
              type="text"
              placeholder="Ім'я користувача"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
              style={inputStyle}
              onFocus={focus}
              onBlur={blur}
            />

            <input
              type="password"
              placeholder="Пароль"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
              style={inputStyle}
              onFocus={focus}
              onBlur={blur}
            />

            {error && (
              <div
                style={{
                  fontSize: 13,
                  padding: '8px 12px',
                  borderRadius: 6,
                  background: COLORS.dangerBg,
                  border: `1px solid ${COLORS.dangerBorder}`,
                  color: COLORS.dangerText,
                }}
              >
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              style={{
                marginTop: 4,
                padding: '10px',
                borderRadius: 6,
                border: 'none',
                background: loading ? '#93b4f3' : COLORS.accent,
                color: '#fff',
                fontSize: 14,
                fontWeight: 600,
                cursor: loading ? 'not-allowed' : 'pointer',
                transition: 'background 0.15s',
                letterSpacing: '-0.01em',
              }}
              onMouseEnter={(e) => {
                if (!loading) e.currentTarget.style.background = COLORS.accentHover;
              }}
              onMouseLeave={(e) => {
                if (!loading) e.currentTarget.style.background = COLORS.accent;
              }}
            >
              {loading ? 'Pевірка...' : 'Увійти'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
