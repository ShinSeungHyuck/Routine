package com.example.routine.data

import androidx.room.Entity

@Entity(
    tableName = "routine_completions",
    primaryKeys = ["routineId", "completionDate"]
)
data class RoutineCompletion(
    val routineId: Int,
    val completionDate: Long // 밀리초 단위 타임스탬프
)
