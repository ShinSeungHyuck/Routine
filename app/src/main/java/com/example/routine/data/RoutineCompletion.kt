package com.example.routine.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "routine_completions", primaryKeys = ["routineId", "completionDate"])
data class RoutineCompletion(
    val routineId: Int,
    val completionDate: Long // Store date as milliseconds since epoch
)