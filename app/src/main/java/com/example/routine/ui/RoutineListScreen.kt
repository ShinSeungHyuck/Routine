package com.example.routine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun RoutineListScreen(viewModel: RoutineViewModel) {
    val routines by viewModel.allRoutines.collectAsState(initial = emptyList())
 val completedRoutinesToday by viewModel.getRoutineCompletionsForDate(Calendar.getInstance().timeInMillis).collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Routine List") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(routines, key = { it.id }) { routine ->
                val isCompletedToday = remember(completedRoutinesToday) {
 completedRoutinesToday.any { it.routineId == routine.id }
                }
                RoutineItem(routine = routine, isCompleted = isCompletedToday) {
                    viewModel.deleteRoutine(routine.id)
                }
            }
        }
    }
}

@Composable
fun RoutineItem(
    routine: com.example.routine.data.Routine,
 isCompleted: Boolean,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(if (isCompleted) Color.Green.copy(alpha = 0.3f) else Color.Transparent),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = routine.content, style = MaterialTheme.typography.h6)
            Text(text = String.format("%02d:%02d", routine.hour, routine.minute))
        }
        Button(onClick = onDeleteClick) {
            Text("Delete")
        }
    }
}