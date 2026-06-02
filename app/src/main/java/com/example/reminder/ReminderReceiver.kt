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

/**
 * BroadcastReceiver that triggers when a reminder alarm fires.
 * Responsible for displaying the high-priority notification and playing custom audio.
 */
class ReminderReceiver : BroadcastReceiver() {
    companion object {
        // Incrementing version to v8 forces the system to apply new 'no-sound' channel settings.
        const val CHANNEL_ID = "reminder_channel_v8"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val triggeredAction = intent.action
        android.util.Log.d("ReminderReceiver", "Alarm fired! Action: $triggeredAction")

        val reminderId = intent.getIntExtra("reminder_id", -1)
        val policyId = intent.getIntExtra("policy_id", -1)
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: ""
        val type = intent.getStringExtra("type")
        val scheduledTime = intent.getStringExtra("scheduled_time")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val db = ReminderDatabase.getDatabase(context)
        val manager = ReminderManager(context)
        
        // Define a high-priority channel. 
        // We setSound(null, null) so the system notification chime doesn't fight with our custom bell sequence.
        val channel = NotificationChannel(CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "High priority reminder alerts"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            setSound(null, null) 
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)

        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val policy = db.reminderDao().getPolicyById(policyId)
                db.reminderDao().markActivated(reminderId, LocalDateTime.now())
                manager.setWatchdogTimer(reminderId, policyId)

                db.logDao().insertLog(com.example.reminder.data.LogEntry(
                    eventType = "ACTIVATED",
                    policyTitle = title,
                    details = "Alarm triggered at ${LocalDateTime.now()}. 5-min window active."
                ))

                val customUri = policy?.customRingtoneUri
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
                    val toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
                    repeat(ringCount) {
                        toneGenerator.startTone(android.media.ToneGenerator.TONE_DTMF_A, 500)
                        kotlinx.coroutines.delay(1500)
                    }
                    toneGenerator.release()
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderReceiver", "Error in background alert task", e)
            } finally {
                pendingResult.finish()
            }
        }

        val mainIntent = Intent(context, com.example.reminder.ui.MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(context, reminderId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            // Use only VIBRATE for defaults to stop the system from playing its own separate chime.
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE) 
            // REMOVED setFullScreenIntent: This prevents the app from auto-opening as a 
            // second "window" when the screen is on, keeping the alert purely in the notification bar.
            .setContentIntent(contentPendingIntent)
            .setTimeoutAfter(90000)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        // Unique request codes for buttons prevent system-level command overlap.
        if (type == "QUESTION") {
            val yesIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = "ACTION_YES_$reminderId"
                putExtra("reminder_id", reminderId)
                putExtra("policy_id", policyId)
                putExtra("scheduled_time", scheduledTime)
            }
            val yesPendingIntent = PendingIntent.getBroadcast(context, reminderId * 10 + 1, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val noIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = "ACTION_NO_$reminderId"
                putExtra("reminder_id", reminderId)
                putExtra("policy_id", policyId)
                putExtra("scheduled_time", scheduledTime)
            }
            val noPendingIntent = PendingIntent.getBroadcast(context, reminderId * 10 + 2, noIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            builder.addAction(android.R.drawable.ic_input_add, "Yes", yesPendingIntent)
            builder.addAction(android.R.drawable.ic_delete, "No", noPendingIntent)
        } else {
            val okIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = "ACTION_OK_$reminderId"
                putExtra("reminder_id", reminderId)
                putExtra("policy_id", policyId)
                putExtra("scheduled_time", scheduledTime)
            }
            val okPendingIntent = PendingIntent.getBroadcast(context, reminderId * 10 + 3, okIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val passIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = "ACTION_PASS_$reminderId"
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
