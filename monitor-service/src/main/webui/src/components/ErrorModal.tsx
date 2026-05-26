import { useEffect, useRef, useState } from 'react';

const AUTO_CLOSE_MS = 5000;

interface ErrorModalProps {
  message: string;
  onClose: () => void;
}

export default function ErrorModal({ message, onClose }: ErrorModalProps) {
  const [progress, setProgress] = useState(100);
  const startRef = useRef<number>(Date.now());
  const rafRef = useRef<number>(0);

  useEffect(() => {
    startRef.current = Date.now();

    const tick = () => {
      const elapsed = Date.now() - startRef.current;
      const remaining = Math.max(0, 100 - (elapsed / AUTO_CLOSE_MS) * 100);
      setProgress(remaining);

      if (elapsed >= AUTO_CLOSE_MS) {
        onClose();
      } else {
        rafRef.current = requestAnimationFrame(tick);
      }
    };

    rafRef.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafRef.current);
  }, [onClose]);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="error-modal-title"
    >
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative z-10 w-full max-w-md bg-slate-800 border border-red-700/60 rounded-2xl shadow-2xl shadow-black/50 overflow-hidden">
        {/* Progress bar */}
        <div className="h-1 bg-slate-700">
          <div
            className="h-full bg-red-500 transition-none"
            style={{ width: `${progress}%` }}
          />
        </div>

        {/* Header */}
        <div className="flex items-center gap-3 px-5 py-4 bg-red-900/30 border-b border-red-700/40">
          <span className="text-2xl select-none" aria-hidden>⚠️</span>
          <h2
            id="error-modal-title"
            className="text-lg font-semibold text-red-300 flex-1"
          >
            Помилка
          </h2>
          <button
            onClick={onClose}
            aria-label="Закрити"
            className="text-slate-400 hover:text-white transition-colors rounded-lg p-1 hover:bg-slate-700"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="px-5 py-5">
          <p className="text-slate-200 leading-relaxed break-words">{message}</p>
        </div>

        {/* Footer */}
        <div className="px-5 pb-5 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 bg-red-700 hover:bg-red-600 active:bg-red-800 text-white font-medium rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 focus:ring-offset-slate-800"
          >
            Зрозуміло
          </button>
        </div>
      </div>
    </div>
  );
}
