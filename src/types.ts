export type Mode = 'FOCUS' | 'SHORT_BREAK' | 'LONG_BREAK';

export interface PomodoroSettings {
  focusMinutes: number;
  shortMinutes: number;
  longMinutes: number;
  sessionsBeforeLong: number;
  soundEnabled: boolean;
  notificationsEnabled: boolean;
}

export const DEFAULT_SETTINGS: PomodoroSettings = {
  focusMinutes: 25,
  shortMinutes: 5,
  longMinutes: 15,
  sessionsBeforeLong: 4,
  soundEnabled: true,
  notificationsEnabled: false,
};
