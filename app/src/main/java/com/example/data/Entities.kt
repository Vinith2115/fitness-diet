package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val gender: String = "Other",
    val experienceLevel: String = "Beginner", // "Beginner", "Intermediate", "Advanced"
    val fitnessGoal: String = "Stay Fit",     // "Lose Weight", "Build Muscle", "Build Endurance", "Stay Fit"
    val dailyCalorieTarget: Int = 2000,
    val dailyProteinTarget: Int = 120, // in grams
    val targetWorkoutMinutes: Int = 30,
    val selectedReminderTime: String = "08:00", // "HH:MM"
    val currentWeightKg: Float = 70f,
    val targetWeightKg: Float = 68f,
    val heightCm: Float = 170f,
    val isOnboarded: Boolean = false
) {
    val intensityMultiplier: Float
        get() = when (experienceLevel) {
            "Beginner" -> 1.0f
            "Intermediate" -> 1.25f
            "Advanced" -> 1.5f
            else -> 1.0f
        }
}

@Entity(tableName = "workout_log")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "YYYY-MM-DD"
    val workoutDayName: String, // e.g. "Day 1: Core Strength"
    val exerciseName: String,
    val repsOrMinutes: String, // e.g. "12 reps" or "45s"
    val caloriesBurned: Int,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "diet_log")
data class DietLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "YYYY-MM-DD"
    val mealType: String, // "Breakfast", "Lunch", "Snack", "Dinner"
    val foodName: String,
    val calories: Int,
    val proteinGrams: Int,
    val isEaten: Boolean = false
)

@Entity(tableName = "weight_log")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "YYYY-MM-DD"
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis()
)
