package com.example.routine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.routine.ui.theme.RoutineTheme
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.routine.data.Routine
import com.example.routine.data.RoutineDao
import com.example.routine.data.RoutineDatabase
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoutineTheme {
                RoutineApp() // Main composable for navigation
            }
        }
    }
}

@Composable
fun RoutineApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember { RoutineDatabase.getDatabase(context) }
    val routineDao = remember { db.routineDao() }

    NavHost(navController = navController, startDestination = RoutineDestinations.ROUTINE_LIST_ROUTE) {
        composable(RoutineDestinations.ROUTINE_LIST_ROUTE) {
            RoutineListScreen(
                routineDao = routineDao,
                onNavigateToCalendar = { navController.navigate(RoutineDestinations.CALENDAR_ROUTE) }
            )
        }
        composable(RoutineDestinations.CALENDAR_ROUTE) {
            CalendarScreen(
                routineDao = routineDao,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    routineDao: RoutineDao,
    onNavigateToCalendar: () -> Unit
) { // Separate composable for routine list
    var routineText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    val routines by routineDao.getAllRoutines().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Routine") },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
                    }
                })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = routineText,
                onValueChange = { routineText = it },
                label = { Text("루틴 내용을 입력하세요") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
                label = { Text("시간을 입력하세요 (HH:mm)") }, // Indicate expected format
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (routineText.isNotBlank() && timeText.isNotBlank()) {
                        // Parse the time string
                        val timeParts = timeText.split(":")
                        if (timeParts.size == 2) {
                            try {
                                val hour = timeParts[0].toInt()
                                val minute = timeParts[1].toInt()

                                // Calculate the delay until the notification time
                                val calendar = Calendar.getInstance()
                                val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
                                val nowMinute = calendar.get(Calendar.MINUTE)

                                var delayInMinutes = (hour * 60 + minute) - (nowHour * 60 + nowMinute)
                                if (delayInMinutes < 0) {
                                    // If the time is in the past, schedule for the next day
                                    delayInMinutes += 24 * 60
                                }

                                val delayInMillis = delayInMinutes * 60 * 1000L

                                // Insert routine into database
                                coroutineScope.launch {
                                    val newRoutine = Routine(name = routineText, time = timeText)
                                    val routineId = routineDao.insertRoutine(newRoutine).toInt()

                                    // Create input data for the worker, including routine ID
                                    val inputData = Data.Builder()
                                        .putString("routineName", routineText)
                                        .putInt("routineId", routineId)
                                        .build()

                                    // Create a OneTimeWorkRequest
                                    val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                                        .setInputData(inputData)
                                        .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                                        .addTag(routineId.toString()) // Add tag to identify the work
                                        .build()

                                    // Enqueue the work request
                                    WorkManager.getInstance(context).enqueue(notificationWorkRequest)
                                }

                                routineText = ""
                                timeText = ""
                            } catch (e: NumberFormatException) {
                                // Handle invalid time format
                                // You might want to show a Toast to the user
                            }
                        } else {
                             // Handle invalid time format
                             // You might want to show a Toast to the user
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("루틴 추가")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(routines) { routine ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("루틴: ${routine.name}")
                            Text("시간: ${routine.time}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    routineDao: RoutineDao,
    onNavigateBack: () -> Unit
) { // Accept RoutineDao and navigate back lambda
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Calendar screen implementation will go here
            Text("Calendar Screen") // Placeholder
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoutineScreenPreview() {
    RoutineTheme {
        // RoutineListScreen(routineDao = FakeRoutineDao(), onNavigateToCalendar = {}) // Preview needs dependencies
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    RoutineTheme {
        // CalendarScreen(routineDao = FakeRoutineDao(), onNavigateBack = {}) // Preview needs dependencies
    }
}