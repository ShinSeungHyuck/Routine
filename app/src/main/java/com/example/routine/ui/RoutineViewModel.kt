package com.example.routine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routine.data.Routine
import com.example.routine.data.RoutineCompletion
import com.example.routine.data.RoutineDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RoutineViewModel(private val routineDao: RoutineDao) : ViewModel() {

    fun insertRoutine(routine: Routine) {
        viewModelScope.launch {
            routineDao.insertRoutine(routine)
        }
    }

    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines()

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            routineDao.deleteRoutine(routineId)
        }
    }

    fun getRoutineCompletionsForDate(completionDate: Long): Flow<List<RoutineCompletion>> {
        return routineDao.getRoutineCompletionsForDate(completionDate)
    }
}