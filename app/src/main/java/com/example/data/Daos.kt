package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)
}

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_log WHERE date = :date ORDER BY timestamp ASC")
    fun getLogsForDate(date: String): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLog)

    @Delete
    suspend fun deleteLog(log: WorkoutLog)
}

@Dao
interface DietLogDao {
    @Query("SELECT * FROM diet_log WHERE date = :date ORDER BY id ASC")
    fun getDietLogsForDate(date: String): Flow<List<DietLog>>

    @Query("SELECT * FROM diet_log WHERE date = :date ORDER BY id ASC")
    suspend fun getDietLogsForDateSync(date: String): List<DietLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietLog(log: DietLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietLogs(logs: List<DietLog>)

    @Update
    suspend fun updateDietLog(log: DietLog)

    @Delete
    suspend fun deleteDietLog(log: DietLog)
}

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_log ORDER BY date ASC, timestamp ASC")
    fun getWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog)

    @Delete
    suspend fun deleteWeightLog(log: WeightLog)
}
