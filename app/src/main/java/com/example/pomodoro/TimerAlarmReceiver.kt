package com.example.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TIMER_EXPIRED = "com.example.pomodoro.ACTION_TIMER_EXPIRED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TIMER_EXPIRED) {
            val prefs = TimerPreferences(context)

            // Prevent duplicate notifications if already notified for this target end time
            val targetEndTime = prefs.targetEndTime
            if (targetEndTime <= 0L || targetEndTime == prefs.lastNotifiedEndTime) {
                return
            }
            prefs.lastNotifiedEndTime = targetEndTime

            val finishedSession = prefs.currentSessionType
            val nextSession = prefs.getNextSessionType()

            // Increment completed count if a focus session concluded
            if (finishedSession == SessionType.FOCUS) {
                prefs.completedCount = prefs.completedCount + 1
            }

            // Show local notification with clear session details
            NotificationHelper.showSessionFinishedNotification(context, finishedSession, nextSession)

            // Automatically transition to the next session
            val nextDuration = prefs.getDurationFor(nextSession)
            prefs.currentSessionType = nextSession
            prefs.timerStatus = TimerStatus.STOPPED
            prefs.remainingMillis = nextDuration
            prefs.totalSessionMillis = nextDuration
            prefs.targetEndTime = 0L

            // Update ViewModel instance if app is currently in foreground
            PomodoroViewModel.syncActiveInstance(context)
        }
    }
}
