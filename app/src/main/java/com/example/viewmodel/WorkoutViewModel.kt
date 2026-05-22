package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.receiver.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private var timerJob: Job? = null

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

    // Workout Guide Timer Player logic
    fun startExerciseSession(exercise: Exercise, workoutDay: WorkoutDay) {
        timerJob?.cancel()
        activeExercise = exercise
        activeWorkoutDay = workoutDay
        timerRemainingSeconds = exercise.durationSeconds
        isTimerRunning = true
        isGuideActive = true

        timerJob = viewModelScope.launch {
            while (timerRemainingSeconds > 0 && isTimerRunning) {
                delay(1000)
                timerRemainingSeconds--
            }
            if (timerRemainingSeconds == 0) {
                isTimerRunning = false
                // Auto complete and log
                logExerciseCompleted(exercise, workoutDay)
            }
        }
    }

    fun toggleTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            timerJob?.cancel()
        } else {
            isTimerRunning = true
            val exercise = activeExercise ?: return
            val workoutDay = activeWorkoutDay ?: return
            timerJob = viewModelScope.launch {
                while (timerRemainingSeconds > 0 && isTimerRunning) {
                    delay(1000)
                    timerRemainingSeconds--
                }
                if (timerRemainingSeconds == 0) {
                    isTimerRunning = false
                    logExerciseCompleted(exercise, workoutDay)
                }
            }
        }
    }

    fun stopExerciseSession() {
        isTimerRunning = false
        timerJob?.cancel()
        isGuideActive = false
        activeExercise = null
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
}
