import {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from 'react';
import {
  login as apiLogin,
  logout as apiLogout,
  verifyAuth,
} from '../api/auth';
import { getCredentials } from '../api/client';

interface AuthContextValue {
  isAuthenticated: boolean;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!getCredentials()) {
      setLoading(false);
      return;
    }
    verifyAuth()
      .then(setIsAuthenticated)
      .finally(() => setLoading(false));
  }, []);

  const login = async (username: string, password: string) => {
    await apiLogin(username, password);
    setIsAuthenticated(true);
  };

  const logout = () => {
    apiLogout();
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth повинен використовуватися всередині AuthProvider');
  }
  return ctx;
}
