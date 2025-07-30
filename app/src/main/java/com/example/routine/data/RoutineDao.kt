package com.example.routine.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRoutine(routine: Routine): Long

    @Query("SELECT * FROM routines")
    fun getAllRoutines(): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRoutineCompletion(completion: RoutineCompletion)

    @Query("SELECT * FROM routine_completions WHERE completionDate BETWEEN :startOfDayMillis AND :endOfDayMillis")
    fun getCompletionsForDate(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<RoutineCompletion>>

    @Query("SELECT COUNT(*) FROM routine_completions WHERE routineId = :routineId AND completionDate BETWEEN :startOfDayMillis AND :endOfDayMillis")
    suspend fun isRoutineCompletedOnDate(routineId: Int, startOfDayMillis: Long, endOfDayMillis: Long): Int
}