package com.example.receiver

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.Exercise
import com.example.data.WorkoutDay
import com.example.data.WorkoutLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.*

class WorkoutTimerService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "workout_timer_channel"
        const val NOTIFICATION_ID = 2004

        const val ACTION_START = "com.example.action.START_TIMER"
        const val ACTION_TOGGLE = "com.example.action.TOGGLE_TIMER"
        const val ACTION_STOP = "com.example.action.STOP_TIMER"

        const val EXTRA_EXERCISE_ID = "extra_exercise_id"
        const val EXTRA_EXERCISE_NAME = "extra_exercise_name"
        const val EXTRA_EXERCISE_DESC = "extra_exercise_desc"
        const val EXTRA_EXERCISE_REPS = "extra_exercise_reps"
        const val EXTRA_EXERCISE_DURATION = "extra_exercise_duration"
        const val EXTRA_EXERCISE_CALORIES = "extra_exercise_calories"
        const val EXTRA_EXERCISE_MUSCLE = "extra_exercise_muscle"
        const val EXTRA_EXERCISE_TIPS = "extra_exercise_tips"

        const val EXTRA_DAY_ID = "extra_day_id"
        const val EXTRA_DAY_NUM = "extra_day_num"
        const val EXTRA_DAY_NAME = "extra_day_name"
        const val EXTRA_DAY_TYPE = "extra_day_type"
        const val EXTRA_DAY_DESC = "extra_day_desc"

        // State flows for ViewModel/UI binding
        val activeExercise = MutableStateFlow<Exercise?>(null)
        val activeWorkoutDay = MutableStateFlow<WorkoutDay?>(null)
        val isTimerRunning = MutableStateFlow(false)
        val timerRemainingSeconds = MutableStateFlow(0)
        val isGuideActive = MutableStateFlow(false)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val exercise = extractExercise(intent)
                val workoutDay = extractWorkoutDay(intent)
                if (exercise != null && workoutDay != null) {
                    startTimer(exercise, workoutDay)
                }
            }
            ACTION_TOGGLE -> {
                toggleTimer()
            }
            ACTION_STOP -> {
                stopTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun extractExercise(intent: Intent): Exercise? {
        val id = intent.getStringExtra(EXTRA_EXERCISE_ID) ?: return null
        val name = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: ""
        val desc = intent.getStringExtra(EXTRA_EXERCISE_DESC) ?: ""
        val reps = intent.getStringExtra(EXTRA_EXERCISE_REPS) ?: ""
        val duration = intent.getIntExtra(EXTRA_EXERCISE_DURATION, 0)
        val calories = intent.getIntExtra(EXTRA_EXERCISE_CALORIES, 0)
        val muscle = intent.getStringExtra(EXTRA_EXERCISE_MUSCLE) ?: ""
        val tips = intent.getStringExtra(EXTRA_EXERCISE_TIPS) ?: ""
        return Exercise(id, name, desc, reps, duration, calories, emptyList(), muscle, tips)
    }

    private fun extractWorkoutDay(intent: Intent): WorkoutDay? {
        val id = intent.getStringExtra(EXTRA_DAY_ID) ?: return null
        val num = intent.getIntExtra(EXTRA_DAY_NUM, 1)
        val name = intent.getStringExtra(EXTRA_DAY_NAME) ?: ""
        val type = intent.getStringExtra(EXTRA_DAY_TYPE) ?: ""
        val desc = intent.getStringExtra(EXTRA_DAY_DESC) ?: ""
        return WorkoutDay(id, num, name, type, desc, emptyList())
    }

    private fun startTimer(exercise: Exercise, workoutDay: WorkoutDay) {
        timerJob?.cancel()
        activeExercise.value = exercise
        activeWorkoutDay.value = workoutDay
        timerRemainingSeconds.value = exercise.durationSeconds
        isTimerRunning.value = true
        isGuideActive.value = true

        startForegroundServiceNotification(exercise)
        runTimer()
    }

    private fun toggleTimer() {
        val exercise = activeExercise.value ?: return
        if (isTimerRunning.value) {
            isTimerRunning.value = false
            timerJob?.cancel()
            updateNotification(exercise, "Paused")
        } else {
            isTimerRunning.value = true
            startForegroundServiceNotification(exercise)
            runTimer()
        }
    }

    private fun runTimer() {
        timerJob = serviceScope.launch {
            while (timerRemainingSeconds.value > 0 && isTimerRunning.value) {
                delay(1000)
                timerRemainingSeconds.value--
                val exercise = activeExercise.value
                if (exercise != null) {
                    updateNotification(exercise, formatTime(timerRemainingSeconds.value))
                }
            }
            if (timerRemainingSeconds.value == 0) {
                isTimerRunning.value = false
                val exercise = activeExercise.value
                val day = activeWorkoutDay.value
                if (exercise != null && day != null) {
                    saveWorkoutLog(exercise, day)
                }
                stopTimer()
            }
        }
    }

    private fun saveWorkoutLog(exercise: Exercise, workoutDay: WorkoutDay) {
        serviceScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(applicationContext)
            val profile = database.userProfileDao().getUserProfileSync()
            val multi = profile?.intensityMultiplier ?: 1.0f
            val calculatedCalories = (exercise.baseCalories * multi).toInt()

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = formatter.format(Date())

            val log = WorkoutLog(
                date = dateStr,
                workoutDayName = workoutDay.name,
                exerciseName = exercise.name,
                repsOrMinutes = exercise.defaultRepsOrTime,
                caloriesBurned = calculatedCalories,
                durationSeconds = exercise.durationSeconds
            )
            database.workoutLogDao().insertLog(log)
        }
    }

    private fun stopTimer() {
        isTimerRunning.value = false
        timerJob?.cancel()
        isGuideActive.value = false
        activeExercise.value = null
        activeWorkoutDay.value = null
        stopForeground(true)
        stopSelf()
    }

    private fun startForegroundServiceNotification(exercise: Exercise) {
        val notification = buildNotification(exercise, formatTime(timerRemainingSeconds.value))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(exercise: Exercise, text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(exercise, text))
    }

    private fun buildNotification(exercise: Exercise, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val toggleIntent = Intent(this, WorkoutTimerService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val stopIntent = Intent(this, WorkoutTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val toggleActionTitle = if (isTimerRunning.value) "Pause" else "Resume"
        val toggleIcon = if (isTimerRunning.value) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Active Workout: ${exercise.name}")
            .setContentText("Time remaining: $text")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(toggleIcon, toggleActionTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Workout Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the remaining time of your active exercise workout session."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
