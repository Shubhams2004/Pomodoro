import React, { useState, useEffect, useRef, useCallback } from 'react';
import confetti from 'canvas-confetti';
import { Mode, PomodoroSettings, DEFAULT_SETTINGS } from './types';
import { TimerScreen } from './components/TimerScreen';
import { SettingsScreen } from './components/SettingsScreen';
import { soundManager } from './utils/sound';

const STORAGE_KEY_SETTINGS = 'pomodoro_settings';
const STORAGE_KEY_COMPLETED = 'pomodoro_completed';

export const App: React.FC = () => {
  // Settings with persistence
  const [settings, setSettings] = useState<PomodoroSettings>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY_SETTINGS);
      if (saved) {
        return { ...DEFAULT_SETTINGS, ...JSON.parse(saved) };
      }
    } catch {
      // Fallback
    }
    return DEFAULT_SETTINGS;
  });

  // Core state matching MainActivity.kt
  const [mode, setMode] = useState<Mode>('FOCUS');
  const [completed, setCompleted] = useState<number>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY_COMPLETED);
      if (saved) return parseInt(saved, 10) || 0;
    } catch {
      // Fallback
    }
    return 0;
  });
  const [running, setRunning] = useState<boolean>(false);
  const [remaining, setRemaining] = useState<number>(() => settings.focusMinutes * 60);
  const [showSettings, setShowSettings] = useState<boolean>(false);

  // Save settings when changed
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY_SETTINGS, JSON.stringify(settings));
    } catch {
      // ignore
    }
  }, [settings]);

  // Save completed when changed
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY_COMPLETED, completed.toString());
    } catch {
      // ignore
    }
  }, [completed]);

  // Total seconds for current mode (for progress ring calculation)
  const getTotalSecondsForMode = useCallback((m: Mode): number => {
    switch (m) {
      case 'FOCUS':
        return settings.focusMinutes * 60;
      case 'SHORT_BREAK':
        return settings.shortMinutes * 60;
      case 'LONG_BREAK':
        return settings.longMinutes * 60;
    }
  }, [settings.focusMinutes, settings.shortMinutes, settings.longMinutes]);

  const resetTimer = useCallback((targetMode: Mode = mode) => {
    const total = getTotalSecondsForMode(targetMode);
    setRemaining(total);
    setRunning(false);
  }, [mode, getTotalSecondsForMode]);

  // Next mode transition matching Kotlin MainActivity.kt
  const nextMode = useCallback(() => {
    if (settings.soundEnabled) {
      soundManager.playCompletionChime();
    }

    if (mode === 'FOCUS') {
      const newCompleted = completed + 1;
      setCompleted(newCompleted);

      // Trigger celebratory confetti on focus finish
      try {
        confetti({
          particleCount: 60,
          spread: 70,
          origin: { y: 0.6 },
        });
      } catch {
        // ignore
      }

      const nextTargetMode: Mode = newCompleted % settings.sessionsBeforeLong === 0 ? 'LONG_BREAK' : 'SHORT_BREAK';
      setMode(nextTargetMode);

      // Trigger Notification if enabled
      if (settings.notificationsEnabled && 'Notification' in window && Notification.permission === 'granted') {
        new Notification('Focus session completed! 🍅', {
          body: nextTargetMode === 'LONG_BREAK' ? 'Time for a well-deserved long break!' : 'Time for a short break!',
          icon: '/favicon.ico',
        });
      }

      setRemaining(getTotalSecondsForMode(nextTargetMode));
      setRunning(false);
    } else {
      setMode('FOCUS');

      if (settings.notificationsEnabled && 'Notification' in window && Notification.permission === 'granted') {
        new Notification('Break ended! ⏰', {
          body: 'Ready to focus again?',
          icon: '/favicon.ico',
        });
      }

      setRemaining(getTotalSecondsForMode('FOCUS'));
      setRunning(false);
    }
  }, [mode, completed, settings, getTotalSecondsForMode]);

  // Countdown timer effect matching Kotlin LaunchedEffect
  const remainingRef = useRef(remaining);
  remainingRef.current = remaining;
  const runningRef = useRef(running);
  runningRef.current = running;

  useEffect(() => {
    if (!running) return;

    const timer = setInterval(() => {
      setRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          // Trigger next mode on zero
          setTimeout(() => {
            nextMode();
          }, 0);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [running, nextMode]);

  // Sync tab title with timer status
  useEffect(() => {
    const mins = Math.floor(remaining / 60);
    const secs = remaining % 60;
    const timeStr = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
    const modeLabel = mode === 'FOCUS' ? 'Focus' : mode === 'SHORT_BREAK' ? 'Short Break' : 'Long Break';

    if (running) {
      document.title = `(${timeStr}) ${modeLabel} - Pomodoro`;
    } else {
      document.title = 'Pomodoro Timer';
    }
  }, [remaining, mode, running]);

  // Keyboard shortcuts (Space to toggle, R to reset, Esc to back from settings)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Don't trigger if user is interacting with an input
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) {
        return;
      }

      if (e.code === 'Space') {
        e.preventDefault();
        setRunning((prev) => !prev);
      } else if (e.code === 'KeyR') {
        e.preventDefault();
        resetTimer(mode);
      } else if (e.code === 'Escape' && showSettings) {
        e.preventDefault();
        setShowSettings(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [mode, showSettings, resetTimer]);

  // Update specific setting and adjust active timer if paused in that mode
  const handleUpdateSetting = <K extends keyof PomodoroSettings>(key: K, value: PomodoroSettings[K]) => {
    setSettings((prev) => {
      const updated = { ...prev, [key]: value };

      if (!running) {
        if (key === 'focusMinutes' && mode === 'FOCUS') {
          setRemaining((value as number) * 60);
        } else if (key === 'shortMinutes' && mode === 'SHORT_BREAK') {
          setRemaining((value as number) * 60);
        } else if (key === 'longMinutes' && mode === 'LONG_BREAK') {
          setRemaining((value as number) * 60);
        }
      }
      return updated;
    });
  };

  const handleSelectMode = (newMode: Mode) => {
    if (newMode === mode) return;
    setMode(newMode);
    setRemaining(getTotalSecondsForMode(newMode));
    setRunning(false);
  };

  const handleToggleSound = () => {
    setSettings((prev) => ({ ...prev, soundEnabled: !prev.soundEnabled }));
  };

  const handleRequestNotifications = async () => {
    if (!('Notification' in window)) {
      alert('This browser does not support desktop notifications.');
      return;
    }

    try {
      const permission = await Notification.requestPermission();
      if (permission === 'granted') {
        setSettings((prev) => ({ ...prev, notificationsEnabled: true }));
      } else {
        setSettings((prev) => ({ ...prev, notificationsEnabled: false }));
      }
    } catch {
      // ignore
    }
  };

  const handleResetCompleted = () => {
    setCompleted(0);
  };

  const handleResetDefaults = () => {
    setSettings(DEFAULT_SETTINGS);
    if (!running) {
      if (mode === 'FOCUS') setRemaining(DEFAULT_SETTINGS.focusMinutes * 60);
      else if (mode === 'SHORT_BREAK') setRemaining(DEFAULT_SETTINGS.shortMinutes * 60);
      else if (mode === 'LONG_BREAK') setRemaining(DEFAULT_SETTINGS.longMinutes * 60);
    }
  };

  return (
    <div className="min-h-screen bg-stone-50 flex flex-col justify-center items-center text-stone-900 transition-colors">
      {showSettings ? (
        <SettingsScreen
          settings={settings}
          onUpdateSetting={handleUpdateSetting}
          onResetCompleted={handleResetCompleted}
          onResetDefaults={handleResetDefaults}
          onRequestNotifications={handleRequestNotifications}
          onBack={() => setShowSettings(false)}
        />
      ) : (
        <TimerScreen
          mode={mode}
          remaining={remaining}
          totalSeconds={getTotalSecondsForMode(mode)}
          completed={completed}
          sessionsBeforeLong={settings.sessionsBeforeLong}
          running={running}
          soundEnabled={settings.soundEnabled}
          onToggle={() => setRunning((prev) => !prev)}
          onReset={() => resetTimer(mode)}
          onSkip={nextMode}
          onToggleSound={handleToggleSound}
          onSettings={() => setShowSettings(true)}
          onSelectMode={handleSelectMode}
        />
      )}
    </div>
  );
};

export default App;
