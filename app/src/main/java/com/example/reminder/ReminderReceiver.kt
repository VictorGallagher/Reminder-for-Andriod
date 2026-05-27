package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.reminder.data.ReminderDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        // Incrementing the version ensures the system re-creates the channel with 
        // the latest high-importance and sound-disabling settings.
        const val CHANNEL_ID = "reminder_channel_v5"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        android.util.Log.d("ReminderReceiver", "Alarm triggered: $action")

        val reminderId = intent.getIntExtra("reminder_id", -1)
        val policyId = intent.getIntExtra("policy_id", -1)
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: ""
        val type = intent.getStringExtra("type")
        val scheduledTime = intent.getStringExtra("scheduled_time")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val db = ReminderDatabase.getDatabase(context)
        
        // High importance is required for the notification to pop up (Heads-up).
        val channel = NotificationChannel(CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "High priority reminders"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            setSound(null, null) // Sound is handled manually by MediaPlayer/ToneGenerator
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)

        // goAsync() tells the OS to keep the process alive after onReceive returns, 
        // allowing us to finish our background sound playback.
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val policy = db.reminderDao().getPolicyById(policyId)
                
                // Log activation
                db.logDao().insertLog(com.example.reminder.data.LogEntry(
                    eventType = "ACTIVATED",
                    policyTitle = title,
                    details = "Reminder triggered at ${java.time.LocalDateTime.now()}"
                ))

                val customUri = policy?.customRingtoneUri
                
                // Ring count: 3 for Questions, 2 for Notifications.
                val ringCount = if (type == "QUESTION") 3 else 2
                
                try {
                    val soundUri = if (!customUri.isNullOrBlank()) {
                        android.net.Uri.parse(customUri)
                    } else {
                        android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                    }
                    
                    val mediaPlayer = android.media.MediaPlayer().apply {
                        setDataSource(context, soundUri)
                        setAudioAttributes(
                            android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        prepare()
                    }

                    repeat(ringCount) {
                        mediaPlayer.start()
                        kotlinx.coroutines.delay(mediaPlayer.duration.toLong() + 800)
                    }
                    mediaPlayer.release()
                } catch (e: Exception) {
                    // Fail-safe "bell" sound using internal DTMF tones.
                    val toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
                    repeat(ringCount) {
                        toneGenerator.startTone(android.media.ToneGenerator.TONE_DTMF_A, 500)
                        kotlinx.coroutines.delay(1500)
                    }
                    toneGenerator.release()
                }
            } finally {
                pendingResult.finish()
            }
        }

        // The contentIntent handles what happens if the user taps the notification body itself.
        val contentIntent = Intent(context, com.example.reminder.ui.MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(context, reminderId, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE) 
            // setFullScreenIntent is critical: it forces the notification to pop up over 
            // other apps or on the lock screen immediately.
            .setFullScreenIntent(contentPendingIntent, true) 
            .setContentIntent(contentPendingIntent)
            .setTimeoutAfter(90000) // Keep visible as a popup for 90 seconds.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        // Unique request codes (reminderId * 10 + offset) prevent the OS from 
        // confusing 'Yes' with 'No' when multiple reminders fire.
        if (type == "QUESTION") {
            val yesIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = "ACTION_YES"
                putExtra("reminder_id", reminderId)
                putExtra("policy_id", policyId)
                putExtra("scheduled_time", scheduledTime)
            }
            val yesPendingIntent = PendingIntent.getBroadcast(context, reminderId * 10 + 1, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val noIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = "ACTION_NO"
                putExtra("reminder_id", reminderId)
                putExtra("policy_id", policyId)
                putExtra("scheduled_time", scheduledTime)
            }
            val noPendingIntent = PendingIntent.getBroadcast(context, reminderId * 10 + 2, noIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            builder.addAction(android.R.drawable.ic_input_add, "Yes", yesPendingIntent)
            builder.addAction(android.R.drawable.ic_delete, "No", noPendingIntent)
        } else {
            val okIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = "ACTION_OK"
                putExtra("reminder_id", reminderId)
                putExtra("policy_id", policyId)
                putExtra("scheduled_time", scheduledTime)
            }
            val okPendingIntent = PendingIntent.getBroadcast(context, reminderId * 10 + 3, okIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val passIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = "ACTION_PASS"
                putExtra("reminder_id", reminderId)
                putExtra("policy_id", policyId)
                putExtra("scheduled_time", scheduledTime)
            }
            val passPendingIntent = PendingIntent.getBroadcast(context, reminderId * 10 + 4, passIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            builder.addAction(android.R.drawable.ic_input_add, "OK", okPendingIntent)
            builder.addAction(android.R.drawable.ic_delete, "Pass", passPendingIntent)
        }

        notificationManager.notify(reminderId, builder.build())
    }
}
