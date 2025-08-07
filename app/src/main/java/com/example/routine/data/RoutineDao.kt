package com.example.routine.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    // 루틴 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    // 루틴 전체 조회
    @Query("SELECT * FROM routines")
    fun getAllRoutines(): Flow<List<Routine>>

    // 루틴 삭제
    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Int)

    // 루틴 완료 기록 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineCompletion(completion: RoutineCompletion)

    // 특정 날짜 범위 내 루틴 완료 기록 조회
    @Query("""
        SELECT * FROM routine_completions
        WHERE completionDate BETWEEN :startOfDayMillis AND :endOfDayMillis
    """)
    fun getCompletionsForDate(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<RoutineCompletion>>

    // 특정 루틴이 날짜 범위 내 완료되었는지 여부 체크 (완료 횟수)
    @Query("""
        SELECT COUNT(*) FROM routine_completions
        WHERE routineId = :routineId AND completionDate BETWEEN :startOfDayMillis AND :endOfDayMillis
    """)
    suspend fun isRoutineCompletedOnDate(routineId: Int, startOfDayMillis: Long, endOfDayMillis: Long): Int
}
