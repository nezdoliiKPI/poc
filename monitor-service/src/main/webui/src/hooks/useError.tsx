import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import ErrorModal from '../components/ErrorModal';

interface ErrorContextValue {
  showError: (message: string) => void;
}

const ErrorContext = createContext<ErrorContextValue | null>(null);

export function ErrorProvider({ children }: { children: ReactNode }) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const showError = useCallback((message: string) => {
    setErrorMessage(message);
  }, []);

  const handleClose = useCallback(() => {
    setErrorMessage(null);
  }, []);

  return (
    <ErrorContext.Provider value={{ showError }}>
      {children}
      {errorMessage !== null && (
        <ErrorModal message={errorMessage} onClose={handleClose} />
      )}
    </ErrorContext.Provider>
  );
}

export function useError(): ErrorContextValue {
  const ctx = useContext(ErrorContext);
  if (!ctx) {
    throw new Error('useError must be used within an ErrorProvider');
  }
  return ctx;
}
