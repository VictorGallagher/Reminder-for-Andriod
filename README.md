# Reminder for Android

A robust, high-priority reminder and task management application for Android, featuring precise alerting, intelligent snooze (Nag), and custom device sounds.

## Key Features

- **High-Precision Alerts**: Uses `AlarmClock` and `ExactAlarms` logic to ensure reminders trigger precisely on time, even during device Doze/Sleep modes.
- **Intelligent Nag (Snooze)**: Postpone reminders with a single tap. The system automatically handles nagging intervals (15 minutes to 2 hours).
- **5-Minute Response Window**: Includes a "Watchdog" feature. If a notification isn't answered within 5 minutes, the app automatically engages the Nag feature to ensure the task isn't forgotten.
- **Custom Audio Alerts**: 
    - Full support for device ringtones, chimes, and alarms.
    - Specialized Alert Patterns: Reminders ring twice, while Question-type alerts ring three times.
- **Scheduling Conflict Resolver**: Automatically detects overlapping reminders and shifts times in 2-minute increments to ensure distinct alerting.
- **Event Logging**: A built-in history system that tracks every creation, trigger, completion, and skip. Logs can be exported and shared as `.txt` files.
- **Boot Recovery**: Reminders are automatically re-registered with the system after a phone reboot.
- **Compact Dashboard**: A chronologically sorted view of all active policies with clear "Next Execution" countdowns.

## Technical Implementation

- **Language**: Kotlin
- **Database**: Room (with Migrations and Relationship mapping)
- **UI**: ViewBinding with Material Components
- **Concurrency**: Kotlin Coroutines and Flows
- **System Integration**: 
    - `AlarmManager` for precise scheduling.
    - `BroadcastReceivers` for Boot, Watchdog, and Notification Actions.
    - `NotificationChannel` with high-priority importance.
    - `FileProvider` for secure log sharing.

## How to Install

1. Clone this repository.
2. Open in Android Studio (Jellyfish or newer).
3. Grant "Post Notifications" and "Exact Alarm" permissions upon request.
4. (Optional) Disable Battery Optimization for the app via the prompt to ensure maximum alarm reliability.

---
*Created with focus on reliability and user accountability.*
