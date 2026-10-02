package com.example.pomodoro

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class Mode(val title: String) {
    FOCUS("Focus"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

class MainActivity : ComponentActivity() {
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { PomodoroApp() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "pomodoro",
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}

@Composable
fun PomodoroApp() {
    var mode by remember { mutableStateOf(Mode.FOCUS) }
    var focusMinutes by remember { mutableIntStateOf(25) }
    var shortMinutes by remember { mutableIntStateOf(5) }
    var longMinutes by remember { mutableIntStateOf(15) }
    var sessionsBeforeLong by remember { mutableIntStateOf(4) }
    var completed by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var remaining by remember { mutableLongStateOf(25L * 60L) }
    var showSettings by remember { mutableStateOf(false) }

    fun resetTimer() {
        remaining = when (mode) {
            Mode.FOCUS -> focusMinutes * 60L
            Mode.SHORT_BREAK -> shortMinutes * 60L
            Mode.LONG_BREAK -> longMinutes * 60L
        }
        running = false
    }

    fun nextMode() {
        if (mode == Mode.FOCUS) {
            completed++
            mode = if (completed % sessionsBeforeLong == 0)
                Mode.LONG_BREAK else Mode.SHORT_BREAK
        } else {
            mode = Mode.FOCUS
        }
        resetTimer()
    }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
        if (running && remaining == 0L) {
            nextMode()
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showSettings) {
                SettingsScreen(
                    focusMinutes, shortMinutes, longMinutes, sessionsBeforeLong,
                    onFocus = { focusMinutes = it; if (!running && mode == Mode.FOCUS) resetTimer() },
                    onShort = { shortMinutes = it; if (!running && mode == Mode.SHORT_BREAK) resetTimer() },
                    onLong = { longMinutes = it; if (!running && mode == Mode.LONG_BREAK) resetTimer() },
                    onSessions = { sessionsBeforeLong = it },
                    onBack = { showSettings = false }
                )
            } else {
                TimerScreen(
                    mode = mode,
                    remaining = remaining,
                    completed = completed,
                    running = running,
                    onToggle = { running = !running },
                    onReset = { resetTimer() },
                    onSettings = { showSettings = true }
                )
            }
        }
    }
}

@Composable
fun TimerScreen(
    mode: Mode,
    remaining: Long,
    completed: Int,
    running: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSettings: () -> Unit
) {
    val minutes = remaining / 60
    val seconds = remaining % 60

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🍅 Pomodoro", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onSettings) { Text("Settings") }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mode.title, fontSize = 22.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                "%02d:%02d".format(minutes, seconds),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text("Completed: $completed", fontSize = 16.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onToggle,
                modifier = Modifier.size(width = 220.dp, height = 64.dp),
                shape = CircleShape
            ) {
                Text(if (running) "Pause" else "Start", fontSize = 20.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onReset) {
                Text("Reset")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsScreen(
    focus: Int,
    short: Int,
    long: Int,
    sessions: Int,
    onFocus: (Int) -> Unit,
    onShort: (Int) -> Unit,
    onLong: (Int) -> Unit,
    onSessions: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(28.dp))
        SettingRow("Focus", focus, 1, 120, onFocus)
        SettingRow("Short break", short, 1, 60, onShort)
        SettingRow("Long break", long, 1, 60, onLong)
        SettingRow("Sessions before long break", sessions, 1, 12, onSessions)
    }
}

@Composable
fun SettingRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 17.sp)
            Text("$value min", fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt().coerceIn(min, max)) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min - 1).coerceAtLeast(0)
        )
    }
}
