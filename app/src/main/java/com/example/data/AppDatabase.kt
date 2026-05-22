package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        WorkoutLog::class,
        DietLog::class,
        WeightLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun dietLogDao(): DietLogDao
    abstract fun weightLogDao(): WeightLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_fitness_planner_db"
                )
                .fallbackToDestructiveMigration() // ensures safety during rapid iterations
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
