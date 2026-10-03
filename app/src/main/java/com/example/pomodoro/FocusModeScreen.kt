package com.example.pomodoro

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pomodoro.ui.theme.BluePrimary
import com.example.pomodoro.ui.theme.GreenPrimary
import com.example.pomodoro.ui.theme.RedPrimary

@Composable
fun FocusModeScreen(
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Intercept back gesture to safely exit Focus Mode and restore system bars
    BackHandler(enabled = true) {
        viewModel.exitFocusMode()
    }

    // Hide status/navigation bars and keep screen awake while Focus Mode is active
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val sessionColor = when (uiState.sessionType) {
        SessionType.FOCUS -> RedPrimary
        SessionType.SHORT_BREAK -> GreenPrimary
        SessionType.LONG_BREAK -> BluePrimary
    }

    // Deep distraction-free dark background
    val darkBg = when (uiState.sessionType) {
        SessionType.FOCUS -> Color(0xFF0F0B0B)
        SessionType.SHORT_BREAK -> Color(0xFF0B100C)
        SessionType.LONG_BREAK -> Color(0xFF0A0E13)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        // Top Bar: Exit Focus Mode Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertizontally
        ) {
            Text(
                text = "FOCUS MODE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.45f)
            )

            TextButton(
                onClick = { viewModel.exitFocusMode() }
            ) {
                Text(
                    text = "✕ Exit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Center: Session Label & Large Countdown Display
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = uiState.sessionType.displayName.uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp,
                color = sessionColor
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = uiState.formattedTime,
                fontSize = 82.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        }

        // Bottom Controls: Pause/Resume, Reset, Exit Focus Mode
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Pause/Resume Button
            Button(
                onClick = { viewModel.onMainButtonClick() },
                modifier = Modifier
                    .width(220.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = sessionColor)
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
                    .height(48.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "RESET",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Exit Focus Mode text button for extra accessibility
            TextButton(
                onClick = { viewModel.exitFocusMode() }
            ) {
                Text(
                    text = "Exit Focus Mode",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
