package com.example.routine.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "routine_completions")
data class RoutineCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: Int,
    val completionDate: Long // Store date as milliseconds since epoch
)