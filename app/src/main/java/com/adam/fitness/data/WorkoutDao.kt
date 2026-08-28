package com.adam.fitness.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Delete
    suspend fun delete(workout: WorkoutEntity)

    @Query("DELETE FROM workouts")
    suspend fun clearAll()

    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE startTime >= :since ORDER BY startTime DESC")
    fun observeSince(since: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts ORDER BY startTime DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<WorkoutEntity>>
}
