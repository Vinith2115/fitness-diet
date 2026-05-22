package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val database: AppDatabase) {
    private val profileDao = database.userProfileDao()
    private val workoutLogDao = database.workoutLogDao()
    private val dietLogDao = database.dietLogDao()
    private val weightLogDao = database.weightLogDao()

    val userProfile: Flow<UserProfile?> = profileDao.getUserProfile()

    suspend fun getUserProfileSync(): UserProfile? = profileDao.getUserProfileSync()

    suspend fun saveUserProfile(profile: UserProfile) {
        profileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: UserProfile) {
        profileDao.updateProfile(profile)
    }

    fun getWorkoutLogsForDate(date: String): Flow<List<WorkoutLog>> =
        workoutLogDao.getLogsForDate(date)

    val allWorkoutLogs: Flow<List<WorkoutLog>> = workoutLogDao.getAllLogs()

    suspend fun insertWorkoutLog(log: WorkoutLog) {
        workoutLogDao.insertLog(log)
    }

    suspend fun deleteWorkoutLog(log: WorkoutLog) {
        workoutLogDao.deleteLog(log)
    }

    fun getDietLogsForDate(date: String): Flow<List<DietLog>> =
        dietLogDao.getDietLogsForDate(date)

    suspend fun getDietLogsForDateSync(date: String): List<DietLog> =
        dietLogDao.getDietLogsForDateSync(date)

    suspend fun insertDietLogs(logs: List<DietLog>) {
        dietLogDao.insertDietLogs(logs)
    }

    suspend fun insertDietLog(log: DietLog) {
        dietLogDao.insertDietLog(log)
    }

    suspend fun updateDietLog(log: DietLog) {
        dietLogDao.updateDietLog(log)
    }

    suspend fun deleteDietLog(log: DietLog) {
        dietLogDao.deleteDietLog(log)
    }

    val weightLogs: Flow<List<WeightLog>> = weightLogDao.getWeightLogs()

    suspend fun insertWeightLog(log: WeightLog) {
        weightLogDao.insertWeightLog(log)
    }

    suspend fun deleteWeightLog(log: WeightLog) {
        weightLogDao.deleteWeightLog(log)
    }
}
