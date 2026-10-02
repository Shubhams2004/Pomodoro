package com.example.pomodoro

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SessionType(val displayName: String) {
    FOCUS("Focus"),
    BREAK("Break")
}

enum class TimerStatus {
    STOPPED,
    RUNNING,
    PAUSED
}

const val FOCUS_DURATION_MILLIS = 25 * 60 * 1000L
const val BREAK_DURATION_MILLIS = 5 * 60 * 1000L

data class PomodoroUiState(
    val sessionType: SessionType = SessionType.FOCUS,
    val timerStatus: TimerStatus = TimerStatus.STOPPED,
    val remainingMillis: Long = FOCUS_DURATION_MILLIS,
    val totalSessionMillis: Long = FOCUS_DURATION_MILLIS,
    val completedCount: Int = 0
) {
    val formattedTime: String
        get() {
            // Round up to the next full second so display shows 25:00 initially and switches cleanly
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

class PomodoroViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var endTimeMillis: Long = 0L

    fun onMainButtonClick() {
        when (_uiState.value.timerStatus) {
            TimerStatus.STOPPED, TimerStatus.PAUSED -> startOrResumeTimer()
            TimerStatus.RUNNING -> pauseTimer()
        }
    }

    private fun startOrResumeTimer() {
        val currentRemaining = _uiState.value.remainingMillis
        endTimeMillis = SystemClock.elapsedRealtime() + currentRemaining

        _uiState.update { it.copy(timerStatus = TimerStatus.RUNNING) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(200)
                val now = SystemClock.elapsedRealtime()
                val remaining = (endTimeMillis - now).coerceAtLeast(0L)

                if (remaining <= 0L) {
                    _uiState.update { it.copy(remainingMillis = 0L) }
                    onSessionFinished()
                    break
                } else {
                    _uiState.update { it.copy(remainingMillis = remaining) }
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null

        val now = SystemClock.elapsedRealtime()
        val remaining = (endTimeMillis - now).coerceAtLeast(0L)

        _uiState.update {
            it.copy(
                timerStatus = TimerStatus.PAUSED,
                remainingMillis = remaining
            )
        }
    }

    fun onResetClick() {
        timerJob?.cancel()
        timerJob = null

        val defaultMillis = if (_uiState.value.sessionType == SessionType.FOCUS) {
            FOCUS_DURATION_MILLIS
        } else {
            BREAK_DURATION_MILLIS
        }

        _uiState.update {
            it.copy(
                timerStatus = TimerStatus.STOPPED,
                remainingMillis = defaultMillis,
                totalSessionMillis = defaultMillis
            )
        }
    }

    private fun onSessionFinished() {
        timerJob?.cancel()
        timerJob = null

        val currentState = _uiState.value
        if (currentState.sessionType == SessionType.FOCUS) {
            val newCompleted = currentState.completedCount + 1
            _uiState.update {
                it.copy(
                    sessionType = SessionType.BREAK,
                    remainingMillis = BREAK_DURATION_MILLIS,
                    totalSessionMillis = BREAK_DURATION_MILLIS,
                    completedCount = newCompleted,
                    timerStatus = TimerStatus.RUNNING
                )
            }
            startOrResumeTimer()
        } else {
            _uiState.update {
                it.copy(
                    sessionType = SessionType.FOCUS,
                    remainingMillis = FOCUS_DURATION_MILLIS,
                    totalSessionMillis = FOCUS_DURATION_MILLIS,
                    timerStatus = TimerStatus.RUNNING
                )
            }
            startOrResumeTimer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
