import React from 'react';
import { ArrowLeft, Bell, BellOff, Volume2, VolumeX, RotateCcw } from 'lucide-react';
import { PomodoroSettings } from '../types';

interface SettingsScreenProps {
  settings: PomodoroSettings;
  onUpdateSetting: <K extends keyof PomodoroSettings>(key: K, value: PomodoroSettings[K]) => void;
  onResetCompleted: () => void;
  onResetDefaults: () => void;
  onRequestNotifications: () => void;
  onBack: () => void;
}

interface SettingRowProps {
  label: string;
  value: number;
  min: number;
  max: number;
  unit?: string;
  onChange: (value: number) => void;
}

const SettingRow: React.FC<SettingRowProps> = ({
  label,
  value,
  min,
  max,
  unit = 'min',
  onChange,
}) => {
  return (
    <div className="py-4 border-b border-stone-200/80">
      <div className="flex items-center justify-between mb-2">
        <label className="text-[17px] font-medium text-stone-800">
          {label}
        </label>
        <span className="text-base font-bold text-stone-900 tabular-nums">
          {value} {unit}
        </span>
      </div>
      <div className="flex items-center gap-3">
        <span className="text-xs text-stone-600 w-6 tabular-nums">{min}</span>
        <input
          type="range"
          min={min}
          max={max}
          value={value}
          onChange={(e) => onChange(Number(e.target.value))}
          className="w-full h-2 bg-stone-200 rounded-lg appearance-none cursor-pointer accent-rose-500"
        />
        <span className="text-xs text-stone-600 w-6 text-right tabular-nums">{max}</span>
      </div>
    </div>
  );
};

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  settings,
  onUpdateSetting,
  onResetCompleted,
  onResetDefaults,
  onRequestNotifications,
  onBack,
}) => {
  return (
    <div className="flex flex-col min-h-screen max-w-md mx-auto w-full px-6 py-8">
      {/* Top Header matching Android */}
      <div className="flex items-center gap-3 mb-6">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 px-3 py-1.5 -ml-2 text-stone-700 hover:text-stone-950 hover:bg-stone-200/60 rounded-full font-semibold transition cursor-pointer text-base"
        >
          <ArrowLeft size={18} />
          <span>Back</span>
        </button>
        <h2 className="text-2xl font-bold tracking-tight text-stone-900">
          Settings
        </h2>
      </div>

      {/* Setting Rows matching original Android exact ranges */}
      <div className="flex flex-col flex-1 divide-y divide-stone-100">
        <SettingRow
          label="Focus"
          value={settings.focusMinutes}
          min={1}
          max={120}
          unit="min"
          onChange={(val) => onUpdateSetting('focusMinutes', val)}
        />
        <SettingRow
          label="Short break"
          value={settings.shortMinutes}
          min={1}
          max={60}
          unit="min"
          onChange={(val) => onUpdateSetting('shortMinutes', val)}
        />
        <SettingRow
          label="Long break"
          value={settings.longMinutes}
          min={1}
          max={60}
          unit="min"
          onChange={(val) => onUpdateSetting('longMinutes', val)}
        />
        <SettingRow
          label="Sessions before long break"
          value={settings.sessionsBeforeLong}
          min={1}
          max={12}
          unit="sessions"
          onChange={(val) => onUpdateSetting('sessionsBeforeLong', val)}
        />

        {/* Audio & Alert Preferences */}
        <div className="py-4 border-b border-stone-200/80">
          <h3 className="text-xs uppercase font-bold tracking-wider text-stone-600 mb-3">
            Alerts & Notifications
          </h3>

          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                {settings.soundEnabled ? <Volume2 size={18} className="text-rose-500" /> : <VolumeX size={18} className="text-stone-400" />}
                <span className="text-[15px] font-medium text-stone-800">Sound Chimes</span>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={settings.soundEnabled}
                  onChange={(e) => onUpdateSetting('soundEnabled', e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-stone-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-stone-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-rose-500"></div>
              </label>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                {settings.notificationsEnabled ? <Bell size={18} className="text-rose-500" /> : <BellOff size={18} className="text-stone-400" />}
                <div>
                  <span className="text-[15px] font-medium text-stone-800 block">Desktop Notifications</span>
                  <span className="text-xs text-stone-600">Get notified when timer completes</span>
                </div>
              </div>
              <button
                type="button"
                onClick={onRequestNotifications}
                className={`px-3 py-1 text-xs font-semibold rounded-full border transition cursor-pointer ${
                  settings.notificationsEnabled
                    ? 'border-emerald-500 text-emerald-700 bg-emerald-50'
                    : 'border-stone-300 text-stone-700 hover:bg-stone-100'
                }`}
              >
                {settings.notificationsEnabled ? 'Enabled' : 'Enable'}
              </button>
            </div>
          </div>
        </div>

        {/* Counter & Defaults Management */}
        <div className="py-6 flex flex-col gap-3">
          <button
            type="button"
            onClick={onResetCompleted}
            className="flex items-center justify-center gap-2 w-full py-2.5 text-sm font-medium text-rose-600 hover:text-rose-700 hover:bg-rose-50 border border-rose-200 rounded-xl transition cursor-pointer"
          >
            <span>Reset Completed Count to 0</span>
          </button>

          <button
            type="button"
            onClick={onResetDefaults}
            className="flex items-center justify-center gap-2 w-full py-2.5 text-sm font-medium text-stone-600 hover:text-stone-900 hover:bg-stone-100 border border-stone-200 rounded-xl transition cursor-pointer"
          >
            <RotateCcw size={15} />
            <span>Restore Default Durations</span>
          </button>
        </div>
      </div>
    </div>
  );
};
