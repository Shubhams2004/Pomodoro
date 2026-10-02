import React from 'react';
import { Mode } from '../types';
import { Play, Pause, RotateCcw, Settings as SettingsIcon, SkipForward, Volume2, VolumeX } from 'lucide-react';

interface TimerScreenProps {
  mode: Mode;
  remaining: number; // in seconds
  totalSeconds: number;
  completed: number;
  sessionsBeforeLong: number;
  running: boolean;
  soundEnabled: boolean;
  onToggle: () => void;
  onReset: () => void;
  onSkip: () => void;
  onToggleSound: () => void;
  onSettings: () => void;
  onSelectMode?: (mode: Mode) => void;
}

export const TimerScreen: React.FC<TimerScreenProps> = ({
  mode,
  remaining,
  totalSeconds,
  completed,
  sessionsBeforeLong,
  running,
  soundEnabled,
  onToggle,
  onReset,
  onSkip,
  onToggleSound,
  onSettings,
  onSelectMode,
}) => {
  const minutes = Math.floor(remaining / 60);
  const seconds = remaining % 60;
  const formattedTime = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

  const modeTitle = mode === 'FOCUS' ? 'Focus' : mode === 'SHORT_BREAK' ? 'Short Break' : 'Long Break';

  // Percentage for progress ring (0% when starting, 100% when remaining is 0)
  const progressPercent = totalSeconds > 0 ? Math.min(100, Math.max(0, ((totalSeconds - remaining) / totalSeconds) * 100)) : 0;

  // Mode theme styling
  const theme = {
    FOCUS: {
      accent: 'bg-rose-500 hover:bg-rose-600 text-white',
      accentBorder: 'border-rose-500',
      ringColor: 'stroke-rose-500',
      badgeBg: 'bg-rose-100 text-rose-800',
      tabActive: 'bg-rose-500 text-white shadow-sm',
      tabInactive: 'text-stone-600 hover:bg-stone-200/60',
      icon: '🍅',
    },
    SHORT_BREAK: {
      accent: 'bg-emerald-600 hover:bg-emerald-700 text-white',
      accentBorder: 'border-emerald-600',
      ringColor: 'stroke-emerald-600',
      badgeBg: 'bg-emerald-100 text-emerald-800',
      tabActive: 'bg-emerald-600 text-white shadow-sm',
      tabInactive: 'text-stone-600 hover:bg-stone-200/60',
      icon: '☕',
    },
    LONG_BREAK: {
      accent: 'bg-sky-600 hover:bg-sky-700 text-white',
      accentBorder: 'border-sky-600',
      ringColor: 'stroke-sky-600',
      badgeBg: 'bg-sky-100 text-sky-800',
      tabActive: 'bg-sky-600 text-white shadow-sm',
      tabInactive: 'text-stone-600 hover:bg-stone-200/60',
      icon: '🌴',
    },
  }[mode];

  // Circle dimensions for SVG progress
  const radius = 130;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (progressPercent / 100) * circumference;

  // Session cycle dots
  const currentInCycle = completed % sessionsBeforeLong;

  return (
    <div className="flex flex-col min-h-screen max-w-md mx-auto w-full px-6 py-8 justify-between select-none">
      {/* Top Header matching Android layout */}
      <header className="flex items-center justify-between w-full">
        <div className="flex items-center gap-2">
          <span className="text-2xl" role="img" aria-label="tomato">🍅</span>
          <h1 className="text-[22px] font-bold tracking-tight text-stone-900">
            Pomodoro
          </h1>
        </div>
        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={onToggleSound}
            aria-label={soundEnabled ? 'Mute sound' : 'Unmute sound'}
            title={soundEnabled ? 'Sound is on (Click to mute)' : 'Sound is muted (Click to unmute)'}
            className="p-2 text-stone-600 hover:text-stone-900 hover:bg-stone-200/60 rounded-full transition-colors cursor-pointer"
          >
            {soundEnabled ? <Volume2 size={20} /> : <VolumeX size={20} className="text-stone-400" />}
          </button>
          <button
            type="button"
            onClick={onSettings}
            className="flex items-center gap-1.5 px-3 py-1.5 text-stone-700 hover:text-stone-900 hover:bg-stone-200/60 rounded-full text-sm font-medium transition-colors cursor-pointer"
          >
            <SettingsIcon size={18} />
            <span>Settings</span>
          </button>
        </div>
      </header>

      {/* Mode Switcher Tabs */}
      {onSelectMode && (
        <div className="flex items-center justify-center gap-1 p-1 bg-stone-200/70 rounded-full mt-4 max-w-xs mx-auto text-xs font-semibold">
          <button
            type="button"
            onClick={() => onSelectMode('FOCUS')}
            className={`px-3.5 py-1.5 rounded-full transition-all cursor-pointer ${
              mode === 'FOCUS' ? theme.tabActive : theme.tabInactive
            }`}
          >
            Focus
          </button>
          <button
            type="button"
            onClick={() => onSelectMode('SHORT_BREAK')}
            className={`px-3.5 py-1.5 rounded-full transition-all cursor-pointer ${
              mode === 'SHORT_BREAK' ? theme.tabActive : theme.tabInactive
            }`}
          >
            Short Break
          </button>
          <button
            type="button"
            onClick={() => onSelectMode('LONG_BREAK')}
            className={`px-3.5 py-1.5 rounded-full transition-all cursor-pointer ${
              mode === 'LONG_BREAK' ? theme.tabActive : theme.tabInactive
            }`}
          >
            Long Break
          </button>
        </div>
      )}

      {/* Main Timer Display Section matching Android */}
      <main className="flex flex-col items-center justify-center my-auto py-6">
        <div className="relative flex items-center justify-center w-[300px] h-[300px]">
          {/* Circular Progress Ring */}
          <svg className="w-full h-full -rotate-90 transform" viewBox="0 0 300 300">
            <circle
              cx="150"
              cy="150"
              r={radius}
              className="stroke-stone-200"
              strokeWidth="10"
              fill="transparent"
            />
            <circle
              cx="150"
              cy="150"
              r={radius}
              className={`${theme.ringColor} transition-[stroke-dashoffset] duration-700 ease-linear`}
              strokeWidth="10"
              fill="transparent"
              strokeDasharray={circumference}
              strokeDashoffset={strokeDashoffset}
              strokeLinecap="round"
            />
          </svg>

          {/* Center timer content */}
          <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
            <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-sm font-semibold mb-2 ${theme.badgeBg}`}>
              <span>{theme.icon}</span>
              <span>{modeTitle}</span>
            </span>

            <div className="text-[72px] font-extrabold tracking-tight tabular-nums text-stone-900 leading-none my-1 font-mono">
              {formattedTime}
            </div>

            <div className="mt-3 flex flex-col items-center gap-1.5">
              <span className="text-base text-stone-600 font-medium">
                Completed: <strong className="text-stone-900">{completed}</strong>
              </span>

              {/* Progress towards long break cycle indicator */}
              <div className="flex items-center gap-1.5 mt-1" title={`${currentInCycle} of ${sessionsBeforeLong} focus sessions toward long break`}>
                {Array.from({ length: sessionsBeforeLong }).map((_, i) => (
                  <div
                    key={i}
                    className={`w-2.5 h-2.5 rounded-full transition-all ${
                      i < currentInCycle
                        ? 'bg-rose-500 ring-2 ring-rose-200'
                        : i === currentInCycle && mode === 'FOCUS' && running
                        ? 'bg-rose-300 animate-pulse'
                        : 'bg-stone-300'
                    }`}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Bottom Action Controls matching Android Button specs */}
      <footer className="flex flex-col items-center w-full gap-3 pb-4">
        <button
          type="button"
          onClick={onToggle}
          className={`flex items-center justify-center gap-2.5 w-[220px] h-[64px] rounded-full text-xl font-semibold shadow-lg hover:shadow-xl active:scale-[0.98] transition-all cursor-pointer ${theme.accent}`}
          aria-label={running ? 'Pause timer' : 'Start timer'}
        >
          {running ? <Pause size={24} fill="currentColor" /> : <Play size={24} fill="currentColor" />}
          <span>{running ? 'Pause' : 'Start'}</span>
        </button>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onReset}
            className="flex items-center gap-1.5 px-6 py-2.5 rounded-full border border-stone-300 hover:border-stone-400 bg-white hover:bg-stone-50 text-stone-700 text-sm font-semibold shadow-xs active:scale-[0.98] transition cursor-pointer"
          >
            <RotateCcw size={16} />
            <span>Reset</span>
          </button>

          <button
            type="button"
            onClick={onSkip}
            title="Skip current session and advance to next"
            className="flex items-center gap-1.5 px-4 py-2.5 rounded-full text-stone-500 hover:text-stone-800 hover:bg-stone-200/50 text-sm font-medium transition cursor-pointer"
          >
            <SkipForward size={16} />
            <span>Skip</span>
          </button>
        </div>

        <p className="text-xs text-stone-600 mt-2">
          Press <kbd className="px-1.5 py-0.5 bg-stone-200 rounded text-stone-700 font-mono">Space</kbd> to toggle, <kbd className="px-1.5 py-0.5 bg-stone-200 rounded text-stone-700 font-mono">R</kbd> to reset
        </p>
      </footer>
    </div>
  );
};
