package com.example.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.routine.data.RoutineDao
import com.example.routine.ui.theme.RoutineTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    routineDao: RoutineDao,
    onNavigateBack: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    val routines by routineDao.getAllRoutines().collectAsState(initial = emptyList())

    val firstDayOfCurrentMonth = remember(currentMonth, currentYear) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val lastDayOfCurrentMonth = remember(currentMonth, currentYear) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    val completionsInMonth by routineDao.getCompletionsForDate(
        startOfDayMillis = firstDayOfCurrentMonth.timeInMillis,
        endOfDayMillis = lastDayOfCurrentMonth.timeInMillis
    ).collectAsState(initial = emptyList())

    val daysList = remember(currentMonth, currentYear) {
        val maxDaysInMonth = firstDayOfCurrentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        (1..maxDaysInMonth).toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = SimpleDateFormat("yyyy년 MM월", Locale.getDefault()).format(firstDayOfCurrentMonth.time))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        calendar.add(Calendar.MONTH, -1)
                        currentMonth = calendar.get(Calendar.MONTH)
                        currentYear = calendar.get(Calendar.YEAR)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                    }
                    IconButton(onClick = {
                        calendar.add(Calendar.MONTH, 1)
                        currentMonth = calendar.get(Calendar.MONTH)
                        currentYear = calendar.get(Calendar.YEAR)
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp) // Added padding for better layout
            .fillMaxSize()) { // Fill the available size

            LazyVerticalGrid(
                columns = GridCells.Fixed(7), // 7 days a week
                modifier = Modifier.fillMaxSize(), // Fill the available size
                contentPadding = PaddingValues(0.dp)
            ) {
                items(daysList) { day ->
                    val dayCalendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfDayMillis = dayCalendar.timeInMillis
                    dayCalendar.set(Calendar.HOUR_OF_DAY, 23)
                    dayCalendar.set(Calendar.MINUTE, 59)
                    dayCalendar.set(Calendar.SECOND, 59)
                    dayCalendar.set(Calendar.MILLISECOND, 999)
                    val endOfDayMillis = dayCalendar.timeInMillis

                    val isDayCompleted = remember(completionsInMonth, routines) {
                        if (routines.isEmpty()) {
                            false // No routines to complete
                        } else {
                            val completedRoutineIdsOnDay = completionsInMonth
                                .filter { it.completionDate in startOfDayMillis..endOfDayMillis }
                                .map { it.routineId }
                                .toSet()
                            routines.all { it.id in completedRoutineIdsOnDay }
                        }
                    }

                    DayCard(day = day, isCompleted = isDayCompleted)
                }
            }
        }
    }
}

@Composable
fun DayCard(day: Int, isCompleted: Boolean) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f) // Make it a square
            .border(1.dp, Color.Gray)
            .background(if (isCompleted) Color.Green else Color.White) // Change background based on completion
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = day.toString())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    RoutineTheme {
        // CalendarScreen(routineDao = FakeRoutineDao(), onNavigateBack = {}) // Preview needs dependencies
    }
}