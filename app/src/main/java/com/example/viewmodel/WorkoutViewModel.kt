package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.receiver.ReminderScheduler
import com.example.receiver.WorkoutTimerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkoutRepository
    private val context: Context get() = getApplication()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WorkoutRepository(database)

        // Bind active exercise player guide states to WorkoutTimerService flows
        viewModelScope.launch {
            WorkoutTimerService.activeExercise.collect { activeExercise = it }
        }
        viewModelScope.launch {
            WorkoutTimerService.activeWorkoutDay.collect { activeWorkoutDay = it }
        }
        viewModelScope.launch {
            WorkoutTimerService.isTimerRunning.collect { isTimerRunning = it }
        }
        viewModelScope.launch {
            WorkoutTimerService.timerRemainingSeconds.collect { timerRemainingSeconds = it }
        }
        viewModelScope.launch {
            WorkoutTimerService.isGuideActive.collect { isGuideActive = it }
        }
    }

    // Date navigation
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Reactive streams
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .map { it ?: UserProfile(id = 1, isOnboarded = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workoutLogs: StateFlow<List<WorkoutLog>> = _selectedDate
        .flatMapLatest { date -> repository.getWorkoutLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dietLogs: StateFlow<List<DietLog>> = _selectedDate
        .flatMapLatest { date -> repository.getDietLogsForDate(date) }
        .onEach { logs ->
            // Pre-populate recommended breakfast/lunch/snacks/dinner if empty for this date
            if (logs.isEmpty()) {
                prepopulateDefaultMealsForDate(_selectedDate.value)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightLogs: StateFlow<List<WeightLog>> = repository.weightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkoutLogs: StateFlow<List<WorkoutLog>> = repository.allWorkoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Streak tracker calculation
    val streakDays: StateFlow<Int> = allWorkoutLogs
        .map { logs -> calculateStreak(logs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Onboarding Form States
    var onboardName by mutableStateOf("")
    var onboardGender by mutableStateOf("Female")
    var onboardExpLevel by mutableStateOf("Beginner") // "Beginner", "Intermediate", "Advanced"
    var onboardGoal by mutableStateOf("Stay Fit")     // "Lose Weight", "Build Muscle", "Build Endurance", "Stay Fit"
    var onboardWeight by mutableStateOf("70")
    var onboardTargetWeight by mutableStateOf("68")
    var onboardHeight by mutableStateOf("170")
    var onboardReminderTime by mutableStateOf("08:00") // HH:MM

    // Active Exercise Player Guide States
    var activeWorkoutDay by mutableStateOf<WorkoutDay?>(null)
    var activeExercise by mutableStateOf<Exercise?>(null)
    var isGuideActive by mutableStateOf(false)
    var timerRemainingSeconds by mutableStateOf(0)
    var isTimerRunning by mutableStateOf(false)

    // Prepopulate user profile with temp data for first launch experience
    fun completeOnboarding() {
        val wt = onboardWeight.toFloatOrNull() ?: 70f
        val twt = onboardTargetWeight.toFloatOrNull() ?: 68f
        val ht = onboardHeight.toFloatOrNull() ?: 170f
        val calTarget = when (onboardGoal) {
            "Lose Weight" -> 1800
            "Build Muscle" -> 2700
            "Build Endurance" -> 2400
            else -> 2000
        }
        val protTarget = when (onboardGoal) {
            "Lose Weight" -> 130
            "Build Muscle" -> 160
            "Build Endurance" -> 120
            else -> 110
        }
        val targetMin = when (onboardExpLevel) {
            "Beginner" -> 20
            "Intermediate" -> 35
            "Advanced" -> 50
            else -> 30
        }

        val profile = UserProfile(
            name = onboardName.ifBlank { "Champion" },
            gender = onboardGender,
            experienceLevel = onboardExpLevel,
            fitnessGoal = onboardGoal,
            dailyCalorieTarget = calTarget,
            dailyProteinTarget = protTarget,
            targetWorkoutMinutes = targetMin,
            selectedReminderTime = onboardReminderTime,
            currentWeightKg = wt,
            targetWeightKg = twt,
            heightCm = ht,
            isOnboarded = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile)
            // Log initial weight
            repository.insertWeightLog(WeightLog(date = getTodayDateString(), weightKg = wt))
            // Schedule original reminder
            val components = onboardReminderTime.split(":")
            if (components.size == 2) {
                val h = components[0].toIntOrNull() ?: 8
                val m = components[1].toIntOrNull() ?: 0
                ReminderScheduler.scheduleDailyReminder(context, h, m)
            }
        }
    }

    private fun prepopulateDefaultMealsForDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Confirm database is empty for this date before building
            val existing = repository.getDietLogsForDateSync(date)
            if (existing.isEmpty()) {
                val profile = repository.getUserProfileSync()
                val targetGoal = profile?.fitnessGoal ?: "Stay Fit"
                val mealsToInsert = StaticWorkoutData.defaultMeals
                    .filter { it.goalMatch == targetGoal }
                    .map { meal ->
                        DietLog(
                            date = date,
                            mealType = meal.mealType,
                            foodName = meal.foodName,
                            calories = meal.calories,
                            proteinGrams = meal.proteinGrams,
                            isEaten = false
                        )
                    }
                repository.insertDietLogs(mealsToInsert)
            }
        }
    }

    // Log weight
    fun addWeightLog(weight: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertWeightLog(WeightLog(date = _selectedDate.value, weightKg = weight))
            // Also update current weight on UserProfile
            val profile = repository.getUserProfileSync()
            if (profile != null) {
                repository.updateProfile(profile.copy(currentWeightKg = weight))
            }
        }
    }

    // Edit profile data (settings screen update)
    fun updateProfileData(
        name: String,
        gender: String,
        heightCm: Float,
        currentWeightKg: Float,
        targetWeightKg: Float,
        fitnessGoal: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileSync()
            if (profile != null) {
                val calTarget = when (fitnessGoal) {
                    "Lose Weight" -> 1800
                    "Build Muscle" -> 2700
                    "Build Endurance" -> 2400
                    else -> 2000
                }
                val protTarget = when (fitnessGoal) {
                    "Lose Weight" -> 130
                    "Build Muscle" -> 160
                    "Build Endurance" -> 120
                    else -> 110
                }
                val updatedProfile = profile.copy(
                    name = name,
                    gender = gender,
                    heightCm = heightCm,
                    currentWeightKg = currentWeightKg,
                    targetWeightKg = targetWeightKg,
                    fitnessGoal = fitnessGoal,
                    dailyCalorieTarget = calTarget,
                    dailyProteinTarget = protTarget
                )
                repository.updateProfile(updatedProfile)

                // Log weight in logs
                repository.insertWeightLog(WeightLog(date = getTodayDateString(), weightKg = currentWeightKg))
            }
        }
    }

    // Change selected date
    fun changeSelectedDate(offsetDays: Int) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val current = formatter.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = current
                add(Calendar.DATE, offsetDays)
            }
            _selectedDate.value = formatter.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSpecificDate(dateString: String) {
        _selectedDate.value = dateString
    }

    // Diet functions
    fun addCustomMeal(mealType: String, food: String, cal: Int, protein: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = DietLog(
                date = _selectedDate.value,
                mealType = mealType,
                foodName = food,
                calories = cal,
                proteinGrams = protein,
                isEaten = true
            )
            repository.insertDietLog(log)
        }
    }

    fun toggleMealEaten(log: DietLog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDietLog(log.copy(isEaten = !log.isEaten))
        }
    }

    fun deleteMealLog(log: DietLog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDietLog(log)
        }
    }

    // Workout Logging
    fun logExerciseCompleted(exercise: Exercise, workoutDay: WorkoutDay) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileSync()
            val multi = profile?.intensityMultiplier ?: 1.0f
            val calculatedCalories = (exercise.baseCalories * multi).toInt()

            val log = WorkoutLog(
                date = _selectedDate.value,
                workoutDayName = workoutDay.name,
                exerciseName = exercise.name,
                repsOrMinutes = exercise.defaultRepsOrTime,
                caloriesBurned = calculatedCalories,
                durationSeconds = exercise.durationSeconds
            )
            repository.insertWorkoutLog(log)
        }
    }

    fun deleteWorkoutLog(log: WorkoutLog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorkoutLog(log)
        }
    }

    // Foreground service timer control delegators
    fun startExerciseSession(exercise: Exercise, workoutDay: WorkoutDay) {
        val intent = Intent(context, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_START
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_ID, exercise.id)
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_NAME, exercise.name)
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_DESC, exercise.description)
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_REPS, exercise.defaultRepsOrTime)
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_DURATION, exercise.durationSeconds)
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_CALORIES, exercise.baseCalories)
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_MUSCLE, exercise.muscleGroup)
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_TIPS, exercise.tips)

            putExtra(WorkoutTimerService.EXTRA_DAY_ID, workoutDay.id)
            putExtra(WorkoutTimerService.EXTRA_DAY_NUM, workoutDay.dayNumber)
            putExtra(WorkoutTimerService.EXTRA_DAY_NAME, workoutDay.name)
            putExtra(WorkoutTimerService.EXTRA_DAY_TYPE, workoutDay.type)
            putExtra(WorkoutTimerService.EXTRA_DAY_DESC, workoutDay.description)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun toggleTimer() {
        val intent = Intent(context, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_TOGGLE
        }
        context.startService(intent)
    }

    fun stopExerciseSession() {
        val intent = Intent(context, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_STOP
        }
        context.startService(intent)
    }

    // Reminders settings
    fun updateReminderTime(timeString: String) {
        onboardReminderTime = timeString
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileSync()
            if (profile != null) {
                repository.updateProfile(profile.copy(selectedReminderTime = timeString))
                // Clear and recreate Android Alarm scheduler
                val components = timeString.split(":")
                if (components.size == 2) {
                    val h = components[0].toIntOrNull() ?: 8
                    val m = components[1].toIntOrNull() ?: 0
                    ReminderScheduler.cancelReminder(context)
                    ReminderScheduler.scheduleDailyReminder(context, h, m)
                }
            }
        }
    }

    fun testInstantNotification() {
        ReminderScheduler.triggerInstantNotification(context)
    }

    fun updateExperienceLevel(level: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileSync()
            if (profile != null) {
                val calories = when (level) {
                    "Beginner" -> 2100
                    "Intermediate" -> 2400
                    else -> 2800
                }
                val protein = when (level) {
                    "Beginner" -> 120
                    "Intermediate" -> 145
                    else -> 175
                }
                val minutes = when (level) {
                    "Beginner" -> 30
                    "Intermediate" -> 45
                    else -> 60
                }
                repository.updateProfile(
                    profile.copy(
                        experienceLevel = level,
                        dailyCalorieTarget = calories,
                        dailyProteinTarget = protein,
                        targetWorkoutMinutes = minutes
                    )
                )
            }
        }
    }

    fun resetProfileToOnboarding() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            db.clearAllTables()
            // Reset local onboarding values
            onboardName = ""
            onboardReminderTime = "08:00"
            _selectedDate.value = getTodayDateString()
        }
    }

    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    // Helper to calculate consecutive active workout days streak
    private fun calculateStreak(logs: List<WorkoutLog>): Int {
        if (logs.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val uniqueDates = logs.mapNotNull {
            try {
                dateFormat.parse(it.date)
            } catch (e: Exception) {
                null
            }
        }.map {
            val cal = Calendar.getInstance()
            cal.time = it
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet()

        if (uniqueDates.isEmpty()) return 0

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMs = todayCal.timeInMillis

        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = todayMs
            add(Calendar.DATE, -1)
        }
        val yesterdayMs = yesterdayCal.timeInMillis

        var currentCheckMs = when {
            uniqueDates.contains(todayMs) -> todayMs
            uniqueDates.contains(yesterdayMs) -> yesterdayMs
            else -> return 0
        }

        var streak = 0
        val checkCal = Calendar.getInstance()
        while (uniqueDates.contains(currentCheckMs)) {
            streak++
            checkCal.timeInMillis = currentCheckMs
            checkCal.add(Calendar.DATE, -1)
            currentCheckMs = checkCal.timeInMillis
        }
        return streak
    }
}
