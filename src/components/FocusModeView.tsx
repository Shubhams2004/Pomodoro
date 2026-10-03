import React from 'react';
import { Mode } from '../types';
import { Play, Pause, RotateCcw, Minimize2 } from 'lucide-react';

interface FocusModeViewProps {
  mode: Mode;
  remaining: number;
  running: boolean;
  onToggle: () => void;
  onReset: () => void;
  onExit: () => void;
}

export const FocusModeView: React.FC<FocusModeViewProps> = ({
  mode,
  remaining,
  running,
  onToggle,
  onReset,
  onExit,
}) => {
  const minutes = Math.floor(remaining / 60);
  const seconds = remaining % 60;
  const formattedTime = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

  const modeLabel = mode === 'FOCUS' ? 'FOCUS' : mode === 'SHORT_BREAK' ? 'SHORT BREAK' : 'LONG BREAK';

  const themeColors = {
    FOCUS: {
      text: 'text-rose-500',
      btn: 'bg-rose-600 hover:bg-rose-700 text-white',
      border: 'border-rose-600/30 hover:bg-rose-950/20 text-stone-300',
    },
    SHORT_BREAK: {
      text: 'text-emerald-500',
      btn: 'bg-emerald-600 hover:bg-emerald-700 text-white',
      border: 'border-emerald-600/30 hover:bg-emerald-950/20 text-stone-300',
    },
    LONG_BREAK: {
      text: 'text-blue-500',
      btn: 'bg-blue-600 hover:bg-blue-700 text-white',
      border: 'border-blue-600/30 hover:bg-blue-950/20 text-stone-300',
    },
  }[mode];

  return (
    <div className="fixed inset-0 z-50 bg-stone-950 text-white flex flex-col justify-between items-center p-8 select-none">
      {/* Top Header */}
      <div className="w-full flex justify-between items-center max-w-2xl">
        <span className="text-xs font-bold tracking-widest text-stone-500 uppercase">
          Focus Mode
        </span>
        <button
          type="button"
          onClick={onExit}
          className="flex items-center gap-1.5 px-3 py-1.5 text-stone-400 hover:text-white hover:bg-stone-900 rounded-full text-sm transition-colors cursor-pointer"
        >
          <Minimize2 size={16} />
          <span>Exit</span>
        </button>
      </div>

      {/* Center Countdown */}
      <div className="flex flex-col items-center justify-center my-auto">
        <span className={`text-sm md:text-base font-semibold tracking-widest uppercase mb-4 ${themeColors.text}`}>
          {modeLabel}
        </span>
        <span className="text-7xl md:text-9xl font-mono font-bold tracking-tight text-white tabular-nums">
          {formattedTime}
        </span>
      </div>

      {/* Bottom Controls */}
      <div className="flex flex-col items-center gap-4 w-full max-w-xs">
        <button
          type="button"
          onClick={onToggle}
          className={`w-full py-4 rounded-full font-semibold text-lg flex items-center justify-center gap-2 shadow-lg transition-transform active:scale-95 cursor-pointer ${themeColors.btn}`}
        >
          {running ? (
            <>
              <Pause size={22} />
              <span>Pause</span>
            </>
          ) : (
            <>
              <Play size={22} />
              <span>Resume</span>
            </>
          )}
        </button>

        <button
          type="button"
          onClick={onReset}
          className="w-full py-2.5 rounded-full border border-stone-800 text-stone-400 hover:text-white hover:bg-stone-900 text-sm font-medium flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
        >
          <RotateCcw size={16} />
          <span>Reset</span>
        </button>
      </div>
    </div>
  );
};
