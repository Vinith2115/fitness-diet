package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.util.Calendar

object ReminderScheduler {
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Morning Reminder (Request code 2002)
        val morningIntent = Intent(context, WorkoutReminderReceiver::class.java).apply {
            putExtra("title", "Morning Workout Alert! 🌅🏋️")
            putExtra("message", "Good morning! Time to activate your body. Open the tracker to log today's routine.")
        }
        val morningPendingIntent = PendingIntent.getBroadcast(
            context,
            2002,
            morningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val morningCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            morningCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            morningPendingIntent
        )

        // 2. Evening Reminder (Request code 2003) - scheduled 12 hours later
        val eveningHour = (hour + 12) % 24
        val eveningIntent = Intent(context, WorkoutReminderReceiver::class.java).apply {
            putExtra("title", "Evening Workout & Diet Check! 🌇🥗")
            putExtra("message", "Time for your evening check-in. Log your dinner, target calories, and evening jog!")
        }
        val eveningPendingIntent = PendingIntent.getBroadcast(
            context,
            2003,
            eveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val eveningCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, eveningHour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            eveningCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            eveningPendingIntent
        )
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WorkoutReminderReceiver::class.java)

        // Cancel morning
        val morningPendingIntent = PendingIntent.getBroadcast(
            context,
            2002,
            intent,
            PendingIntent.FLAG_NO_CREATE or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        if (morningPendingIntent != null) {
            alarmManager.cancel(morningPendingIntent)
            morningPendingIntent.cancel()
        }

        // Cancel evening
        val eveningPendingIntent = PendingIntent.getBroadcast(
            context,
            2003,
            intent,
            PendingIntent.FLAG_NO_CREATE or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        if (eveningPendingIntent != null) {
            alarmManager.cancel(eveningPendingIntent)
            eveningPendingIntent.cancel()
        }
    }

    fun triggerInstantNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "workout_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Workout Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1234,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Let's crush today's routine! 🔥")
            .setContentText("Focus on your goal today. Tap to track your core session and view meals!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(9999, notification)
    }
}
