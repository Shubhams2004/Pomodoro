# Pomodoro Timer (Android)

A clean, beginner-friendly offline Android Pomodoro timer built with **Kotlin**, **Jetpack Compose**, **Material 3**, and **ViewModel**.

---

## Features
- **Focus Session**: Default 25 minutes.
- **Break Session**: Default 5 minutes.
- **Automatic Cycling**: Focus (25:00) → Break (05:00) → Focus (25:00) → Break (05:00) → repeat.
- **Dynamic Action Button**:
  - `START` when stopped
  - `PAUSE` when running
  - `RESUME` when paused
- **Reset**: Resets the current session back to its starting time without clearing completed count.
- **Completed Counter**: Increments by 1 every time a Focus session completes.
- **Accurate Timing**: Calculates remaining time based on monotonic system elapsed time (`SystemClock.elapsedRealtime()`) to prevent timer drift.
- **Material 3 & Edge-to-Edge**: Supports both light and dark system themes seamlessly.
- **No Complex Dependencies**: Pure offline app with zero accounts, database, or background services.

---

## Project Structure
```text
.
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/pomodoro/
│       │   ├── MainActivity.kt
│       │   ├── PomodoroViewModel.kt
│       │   ├── PomodoroScreen.kt
│       │   └── ui/theme/
│       │       ├── Color.kt
│       │       ├── Type.kt
│       │       └── Theme.kt
│       └── res/
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               └── themes.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## How to Open in Android Studio
1. Open **Android Studio**.
2. Select **File → Open...** (or click **Open** on the Welcome screen).
3. Browse to and select the root directory of this project.
4. Allow Gradle to sync dependencies and index the project files.

---

## How to Run on an Android Emulator
1. In Android Studio, open the **Device Manager** (Tools → Device Manager).
2. Create or start an Android Virtual Device (AVD) running Android 8.0 (API 26) or higher.
3. Select your emulator in the target device dropdown at the top toolbar.
4. Click the green **Run (▶)** button (or press `Shift + F10`).

---

## How to Run on a Physical Android Phone
1. On your phone, enable **Developer Options**:
   - Go to **Settings → About Phone**.
   - Tap **Build Number** 7 times until you see "You are now a developer!".
2. Under **Settings → System / Developer Options**, enable **USB Debugging**.
3. Connect your phone to your computer via USB cable.
4. Accept the "Allow USB debugging?" prompt on your phone screen.
5. In Android Studio, select your physical phone from the device dropdown.
6. Click **Run (▶)**.

---

## Testing Checklist
- [x] App launches directly into the main screen showing `Pomodoro`, `Focus`, `25:00`, `[ START ]`, `[ RESET ]`, and `Completed: 0`.
- [x] Tapping **START** changes the button to **PAUSE** and the timer counts down.
- [x] Tapping **PAUSE** stops the timer and changes the button to **RESUME**.
- [x] Tapping **RESUME** continues counting down accurately from the paused time.
- [x] Tapping **RESET** restores the timer to 25:00 and sets state to stopped (`START`), without altering `Completed: 0`.
- [x] When Focus reaches `00:00`:
  - `Completed` increments by 1.
  - Session automatically switches to `Break` (5:00).
  - Break timer starts running automatically.
- [x] When Break reaches `00:00`:
  - Session automatically switches back to `Focus` (25:00).
  - Focus timer starts running automatically.
- [x] UI respects system dark mode and light mode cleanly.
