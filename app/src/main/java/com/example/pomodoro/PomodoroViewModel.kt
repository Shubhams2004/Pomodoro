package com.example.pomodoro

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

data class PomodoroUiState(
    val sessionType: SessionType = SessionType.FOCUS,
    val timerStatus: TimerStatus = TimerStatus.STOPPED,
    val remainingMillis: Long = 25 * 60 * 1000L,
    val totalSessionMillis: Long = 25 * 60 * 1000L,
    val completedCount: Int = 0,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val longBreakInterval: Int = 4,
    val isSettingsOpen: Boolean = false,
    val isFocusMode: Boolean = false
) {
    val formattedTime: String
        get() {
            val totalSeconds = if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            return "%02d:%02d".format(minutes, seconds)
        }

    val mainButtonText: String
        get() = when (timerStatus) {
            TimerStatus.STOPPED -> "START"
            TimerStatus.RUNNING -> "PAUSE"
            TimerStatus.PAUSED -> "RESUME"
        }
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = TimerPreferences(application)

    private val _uiState = MutableStateFlow(
        PomodoroUiState(
            sessionType = prefs.currentSessionType,
            timerStatus = prefs.timerStatus,
            remainingMillis = prefs.remainingMillis,
            totalSessionMillis = prefs.totalSessionMillis,
            completedCount = prefs.completedCount,
            focusMinutes = prefs.focusMinutes,
            shortBreakMinutes = prefs.shortBreakMinutes,
            longBreakMinutes = prefs.longBreakMinutes,
            longBreakInterval = prefs.longBreakInterval
        )
    )
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    companion object {
        private var activeInstance: WeakReference<PomodoroViewModel>? = null

        fun syncActiveInstance(context: Context) {
            activeInstance?.get()?.syncFromStorage()
        }
    }

    init {
        activeInstance = WeakReference(this)
        syncFromStorage()
    }

    fun onAppResumed() {
        syncFromStorage()
    }

    fun syncFromStorage() {
        val now = System.currentTimeMillis()
        val targetEndTime = prefs.targetEndTime
        val status = prefs.timerStatus

        if (status == TimerStatus.RUNNING && targetEndTime > 0L) {
            if (now >= targetEndTime) {
                // Timer finished while app was in background or closed
                tickerJob?.cancel()
                tickerJob = null

                // If notification was not yet posted by the alarm receiver, post it once here
                if (targetEndTime != prefs.lastNotifiedEndTime) {
                    prefs.lastNotifiedEndTime = targetEndTime
                    val finishedSession = prefs.currentSessionType
                    val nextSession = prefs.getNextSessionType()

                    if (finishedSession == SessionType.FOCUS) {
                        prefs.completedCount = prefs.completedCount + 1
                    }

                    NotificationHelper.showSessionFinishedNotification(
                        getApplication(),
                        finishedSession,
                        nextSession
                    )

                    val nextDuration = prefs.getDurationFor(nextSession)
                    prefs.currentSessionType = nextSession
                    prefs.timerStatus = TimerStatus.STOPPED
                    prefs.remainingMillis = nextDuration
                    prefs.totalSessionMillis = nextDuration
                    prefs.targetEndTime = 0L
                }

                updateUiFromPrefs()
            } else {
                // Timer is still running, calculate remaining precisely
                val remaining = targetEndTime - now
                prefs.remainingMillis = remaining
                updateUiFromPrefs()
                startForegroundTicker(targetEndTime)
            }
        } else {
            updateUiFromPrefs()
        }
    }

    private fun updateUiFromPrefs() {
        _uiState.update {
            it.copy(
                sessionType = prefs.currentSessionType,
                timerStatus = prefs.timerStatus,
                remainingMillis = prefs.remainingMillis,
                totalSessionMillis = prefs.totalSessionMillis,
                completedCount = prefs.completedCount,
                focusMinutes = prefs.focusMinutes,
                shortBreakMinutes = prefs.shortBreakMinutes,
                longBreakMinutes = prefs.longBreakMinutes,
                longBreakInterval = prefs.longBreakInterval
            )
        }
    }

    fun onMainButtonClick() {
        when (_uiState.value.timerStatus) {
            TimerStatus.STOPPED, TimerStatus.PAUSED -> startOrResumeTimer()
            TimerStatus.RUNNING -> pauseTimer()
        }
    }

    private fun startOrResumeTimer() {
        val remaining = _uiState.value.remainingMillis
        if (remaining <= 0L) return

        val now = System.currentTimeMillis()
        val targetEndTime = now + remaining

        prefs.targetEndTime = targetEndTime
        prefs.timerStatus = TimerStatus.RUNNING
        prefs.remainingMillis = remaining

        TimerAlarmScheduler.scheduleAlarm(getApplication(), targetEndTime)

        _uiState.update { it.copy(timerStatus = TimerStatus.RUNNING) }
        startForegroundTicker(targetEndTime)
    }

    private fun startForegroundTicker(targetEndTime: Long) {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(200)
                val now = System.currentTimeMillis()
                val remaining = (targetEndTime - now).coerceAtLeast(0L)

                if (remaining <= 0L) {
                    tickerJob?.cancel()
                    tickerJob = null

                    // Handle completion
                    if (targetEndTime != prefs.lastNotifiedEndTime) {
                        prefs.lastNotifiedEndTime = targetEndTime
                        val finishedSession = prefs.currentSessionType
                        val nextSession = prefs.getNextSessionType()

                        if (finishedSession == SessionType.FOCUS) {
                            prefs.completedCount = prefs.completedCount + 1
                        }

                        NotificationHelper.showSessionFinishedNotification(
                            getApplication(),
                            finishedSession,
                            nextSession
                        )

                        val nextDuration = prefs.getDurationFor(nextSession)
                        prefs.currentSessionType = nextSession
                        prefs.timerStatus = TimerStatus.STOPPED
                        prefs.remainingMillis = nextDuration
                        prefs.totalSessionMillis = nextDuration
                        prefs.targetEndTime = 0L

                        updateUiFromPrefs()
                    }
                    break
                } else {
                    _uiState.update { it.copy(remainingMillis = remaining) }
                }
            }
        }
    }

    private fun pauseTimer() {
        tickerJob?.cancel()
        tickerJob = null

        val now = System.currentTimeMillis()
        val targetEndTime = prefs.targetEndTime
        val remaining = if (targetEndTime > 0L) {
            (targetEndTime - now).coerceAtLeast(0L)
        } else {
            _uiState.value.remainingMillis
        }

        TimerAlarmScheduler.cancelAlarm(getApplication())

        prefs.timerStatus = TimerStatus.PAUSED
        prefs.remainingMillis = remaining
        prefs.targetEndTime = 0L

        _uiState.update {
            it.copy(
                timerStatus = TimerStatus.PAUSED,
                remainingMillis = remaining
            )
        }
    }

    fun onResetClick() {
        tickerJob?.cancel()
        tickerJob = null

        TimerAlarmScheduler.cancelAlarm(getApplication())

        val duration = prefs.getDurationFor(prefs.currentSessionType)
        prefs.timerStatus = TimerStatus.STOPPED
        prefs.remainingMillis = duration
        prefs.totalSessionMillis = duration
        prefs.targetEndTime = 0L

        updateUiFromPrefs()
    }

    fun onSessionSelect(newType: SessionType) {
        if (_uiState.value.sessionType == newType && _uiState.value.timerStatus != TimerStatus.STOPPED) {
            return
        }

        tickerJob?.cancel()
        tickerJob = null
        TimerAlarmScheduler.cancelAlarm(getApplication())

        val duration = prefs.getDurationFor(newType)
        prefs.currentSessionType = newType
        prefs.timerStatus = TimerStatus.STOPPED
        prefs.remainingMillis = duration
        prefs.totalSessionMillis = duration
        prefs.targetEndTime = 0L

        updateUiFromPrefs()
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun saveSettings(
        focusMin: Int,
        shortBreakMin: Int,
        longBreakMin: Int,
        interval: Int
    ) {
        prefs.focusMinutes = focusMin.coerceIn(1, 120)
        prefs.shortBreakMinutes = shortBreakMin.coerceIn(1, 60)
        prefs.longBreakMinutes = longBreakMin.coerceIn(1, 60)
        prefs.longBreakInterval = interval.coerceIn(1, 12)

        if (prefs.timerStatus == TimerStatus.STOPPED) {
            val duration = prefs.getDurationFor(prefs.currentSessionType)
            prefs.remainingMillis = duration
            prefs.totalSessionMillis = duration
        }

        _uiState.update {
            it.copy(
                focusMinutes = prefs.focusMinutes,
                shortBreakMinutes = prefs.shortBreakMinutes,
                longBreakMinutes = prefs.longBreakMinutes,
                longBreakInterval = prefs.longBreakInterval,
                remainingMillis = prefs.remainingMillis,
                totalSessionMillis = prefs.totalSessionMillis,
                isSettingsOpen = false
            )
        }
    }

    fun enterFocusMode() {
        _uiState.update { it.copy(isFocusMode = true) }
    }

    fun exitFocusMode() {
        _uiState.update { it.copy(isFocusMode = false) }
    }

    fun toggleFocusMode() {
        _uiState.update { it.copy(isFocusMode = !it.isFocusMode) }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        if (activeInstance?.get() == this) {
            activeInstance = null
        }
    }
}
