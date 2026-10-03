package com.example.pomodoro

import android.content.Context
import android.content.SharedPreferences

class TimerPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pomodoro_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FOCUS_MINUTES = "focus_minutes"
        private const val KEY_SHORT_BREAK_MINUTES = "short_break_minutes"
        private const val KEY_LONG_BREAK_MINUTES = "long_break_minutes"
        private const val KEY_LONG_BREAK_INTERVAL = "long_break_interval"

        private const val KEY_SESSION_TYPE = "session_type"
        private const val KEY_TIMER_STATUS = "timer_status"
        private const val KEY_REMAINING_MILLIS = "remaining_millis"
        private const val KEY_TARGET_END_TIME = "target_end_time"
        private const val KEY_TOTAL_SESSION_MILLIS = "total_session_millis"
        private const val KEY_COMPLETED_COUNT = "completed_count"
        private const val KEY_LAST_NOTIFIED_END_TIME = "last_notified_end_time"
    }

    var focusMinutes: Int
        get() = prefs.getInt(KEY_FOCUS_MINUTES, 25)
        set(value) = prefs.edit().putInt(KEY_FOCUS_MINUTES, value).apply()

    var shortBreakMinutes: Int
        get() = prefs.getInt(KEY_SHORT_BREAK_MINUTES, 5)
        set(value) = prefs.edit().putInt(KEY_SHORT_BREAK_MINUTES, value).apply()

    var longBreakMinutes: Int
        get() = prefs.getInt(KEY_LONG_BREAK_MINUTES, 15)
        set(value) = prefs.edit().putInt(KEY_LONG_BREAK_MINUTES, value).apply()

    var longBreakInterval: Int
        get() = prefs.getInt(KEY_LONG_BREAK_INTERVAL, 4)
        set(value) = prefs.edit().putInt(KEY_LONG_BREAK_INTERVAL, value).apply()

    var currentSessionType: SessionType
        get() {
            val name = prefs.getString(KEY_SESSION_TYPE, SessionType.FOCUS.name) ?: SessionType.FOCUS.name
            return try {
                SessionType.valueOf(name)
            } catch (_: Exception) {
                SessionType.FOCUS
            }
        }
        set(value) = prefs.edit().putString(KEY_SESSION_TYPE, value.name).apply()

    var timerStatus: TimerStatus
        get() {
            val name = prefs.getString(KEY_TIMER_STATUS, TimerStatus.STOPPED.name) ?: TimerStatus.STOPPED.name
            return try {
                TimerStatus.valueOf(name)
            } catch (_: Exception) {
                TimerStatus.STOPPED
            }
        }
        set(value) = prefs.edit().putString(KEY_TIMER_STATUS, value.name).apply()

    var remainingMillis: Long
        get() = prefs.getLong(KEY_REMAINING_MILLIS, 25 * 60 * 1000L)
        set(value) = prefs.edit().putLong(KEY_REMAINING_MILLIS, value).apply()

    var targetEndTime: Long
        get() = prefs.getLong(KEY_TARGET_END_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_TARGET_END_TIME, value).apply()

    var totalSessionMillis: Long
        get() = prefs.getLong(KEY_TOTAL_SESSION_MILLIS, 25 * 60 * 1000L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_SESSION_MILLIS, value).apply()

    var completedCount: Int
        get() = prefs.getInt(KEY_COMPLETED_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_COMPLETED_COUNT, value).apply()

    var lastNotifiedEndTime: Long
        get() = prefs.getLong(KEY_LAST_NOTIFIED_END_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_NOTIFIED_END_TIME, value).apply()

    fun getDurationFor(sessionType: SessionType): Long {
        return when (sessionType) {
            SessionType.FOCUS -> focusMinutes * 60 * 1000L
            SessionType.SHORT_BREAK -> shortBreakMinutes * 60 * 1000L
            SessionType.LONG_BREAK -> longBreakMinutes * 60 * 1000L
        }
    }

    fun getNextSessionType(): SessionType {
        return when (currentSessionType) {
            SessionType.FOCUS -> {
                val nextCompleted = completedCount + 1
                if (nextCompleted % longBreakInterval == 0) {
                    SessionType.LONG_BREAK
                } else {
                    SessionType.SHORT_BREAK
                }
            }
            SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> SessionType.FOCUS
        }
    }
}
