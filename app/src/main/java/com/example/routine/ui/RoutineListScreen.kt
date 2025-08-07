package com.example.routine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun RoutineListScreen(
    navController: NavController,            // ← navController 추가
    viewModel: RoutineViewModel
) {
    val calendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val startOfDayMillis = calendar.timeInMillis
    val endOfDayMillis = startOfDayMillis + 24 * 60 * 60 * 1000 - 1

    val routines by viewModel.allRoutines.collectAsState(initial = emptyList())
    val completedRoutinesToday by viewModel.getRoutineCompletionsForDate(startOfDayMillis, endOfDayMillis)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routine List") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("addRoutine")
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Routine")
                    }
                }
            )
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
