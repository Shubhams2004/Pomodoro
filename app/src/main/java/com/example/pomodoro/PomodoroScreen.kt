package com.example.pomodoro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.ui.theme.BluePrimary
import com.example.pomodoro.ui.theme.GreenPrimary
import com.example.pomodoro.ui.theme.RedPrimary

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val sessionColor = when (uiState.sessionType) {
        SessionType.FOCUS -> RedPrimary
        SessionType.SHORT_BREAK -> GreenPrimary
        SessionType.LONG_BREAK -> BluePrimary
    }

    val progress = if (uiState.totalSessionMillis > 0L) {
        (uiState.remainingMillis.toFloat() / uiState.totalSessionMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    if (uiState.isFocusMode) {
        FocusModeScreen(
            viewModel = viewModel,
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Title & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Text(
                    text = "Pomodoro",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    verticalAlignment = Alignment.CenterVertizontally,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Enter Focus Mode Button
                    IconButton(
                        onClick = { viewModel.enterFocusMode() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⛶",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = { viewModel.openSettings() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚙",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Session Mode Tabs: Focus | Short Break | Long Break
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SessionType.entries.forEach { type ->
                    val isSelected = uiState.sessionType == type
                    val tabColor = when (type) {
                        SessionType.FOCUS -> RedPrimary
                        SessionType.SHORT_BREAK -> GreenPrimary
                        SessionType.LONG_BREAK -> BluePrimary
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) tabColor else Color.Transparent)
                            .clickable { viewModel.onSessionSelect(type) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Center Content: Circular Progress Ring & Large Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.sessionType.displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = sessionColor
                )

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(260.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = sessionColor,
                        trackColor = sessionColor.copy(alpha = 0.15f),
                        strokeWidth = 10.dp,
                        strokeCap = StrokeCap.Round
                    )

                    Text(
                        text = uiState.formattedTime,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Action Buttons & Session Counter
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Action Button (START / PAUSE / RESUME)
                Button(
                    onClick = { viewModel.onMainButtonClick() },
                    modifier = Modifier
                        .width(220.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sessionColor
                    )
                ) {
                    Text(
                        text = uiState.mainButtonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reset Button
                OutlinedButton(
                    onClick = { viewModel.onResetClick() },
                    modifier = Modifier
                        .width(220.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "RESET",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Completed Pomodoro Counter
                Text(
                    text = "Completed: ${uiState.completedCount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }
        }
    }

    // Settings Screen Dialog
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            currentFocus = uiState.focusMinutes,
            currentShortBreak = uiState.shortBreakMinutes,
            currentLongBreak = uiState.longBreakMinutes,
            currentInterval = uiState.longBreakInterval,
            onDismiss = { viewModel.closeSettings() },
            onSave = { focus, shortBreak, longBreak, interval ->
                viewModel.saveSettings(focus, shortBreak, longBreak, interval)
            }
        )
    }
}

@Composable
fun SettingsDialog(
    currentFocus: Int,
    currentShortBreak: Int,
    currentLongBreak: Int,
    currentInterval: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int) -> Unit
) {
    var focusVal by remember { mutableFloatStateOf(currentFocus.toFloat()) }
    var shortBreakVal by remember { mutableFloatStateOf(currentShortBreak.toFloat()) }
    var longBreakVal by remember { mutableFloatStateOf(currentLongBreak.toFloat()) }
    var intervalVal by remember { mutableFloatStateOf(currentInterval.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Timer Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Focus Duration
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Focus Time", fontWeight = FontWeight.Medium)
                        Text(text = "${focusVal.toInt()} min", color = RedPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = focusVal,
                        onValueChange = { focusVal = it },
                        valueRange = 1f..60f,
                        steps = 59,
                        colors = SliderDefaults.colors(thumbColor = RedPrimary, activeTrackColor = RedPrimary)
                    )
                }

                // Short Break Duration
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Short Break", fontWeight = FontWeight.Medium)
                        Text(text = "${shortBreakVal.toInt()} min", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = shortBreakVal,
                        onValueChange = { shortBreakVal = it },
                        valueRange = 1f..30f,
                        steps = 29,
                        colors = SliderDefaults.colors(thumbColor = GreenPrimary, activeTrackColor = GreenPrimary)
                    )
                }

                // Long Break Duration
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Long Break", fontWeight = FontWeight.Medium)
                        Text(text = "${longBreakVal.toInt()} min", color = BluePrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = longBreakVal,
                        onValueChange = { longBreakVal = it },
                        valueRange = 1f..45f,
                        steps = 44,
                        colors = SliderDefaults.colors(thumbColor = BluePrimary, activeTrackColor = BluePrimary)
                    )
                }

                // Long Break Interval
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Long Break Every", fontWeight = FontWeight.Medium)
                        Text(text = "${intervalVal.toInt()} sessions", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = intervalVal,
                        onValueChange = { intervalVal = it },
                        valueRange = 1f..10f,
                        steps = 9
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        focusVal.toInt(),
                        shortBreakVal.toInt(),
                        longBreakVal.toInt(),
                        intervalVal.toInt()
                    )
                }
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
