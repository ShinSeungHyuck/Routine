package com.example.routine

import android.app.AlarmManager
import android.os.Bundle
import android.app.PendingIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.activity.compose.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.routine.data.Routine
import com.example.routine.data.RoutineDao
import com.example.routine.data.RoutineDatabase
import com.example.routine.data.RoutineCompletion
import java.util.Date
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.util.Log // Import Log
import androidx.lifecycle.lifecycleScope
// import androidx.compose.material.icons.filled.Delete // Import Delete Icon
import androidx.compose.material.icons.filled.DateRange // Re-import DateRange for clarity
import com.example.routine.ui.theme.RoutineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the activity was launched from a notification click
        val routineIdFromNotification = intent?.getIntExtra("routineId", -1) ?: -1
        if (routineIdFromNotification != -1) {
            // Handle routine completion
            val db = RoutineDatabase.getDatabase(applicationContext)
            val routineDao = db.routineDao()
            lifecycleScope.launch {
                routineDao.insertRoutineCompletion(RoutineCompletion(routineId = routineIdFromNotification, completionDate = Date().time))
            }
        }
        //enableEdgeToEdge()
        setContent {
            RoutineTheme {
                RoutineApp()
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
) {
    var routineText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    val routines by routineDao.getAllRoutines().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Routine") },
                actions = {
                    // 초기화 버튼 (달력 아이콘 사용, 달력 버튼 왼쪽에 위치)
                    IconButton(onClick = {
                        coroutineScope.launch {
                            routineDao.deleteAllRoutines()
                            Log.d("RoutineDebug", "All routines deleted.") // 삭제 확인 로그
                        }
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "모든 루틴 초기화") // 달력 아이콘 사용
                    }
                    // 캘린더 버튼
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                    }
                }
            )
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
                label = { Text("시간을 입력하세요 (HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (routineText.isNotBlank() && timeText.isNotBlank()) {
                        Log.d("RoutineDebug", "Input Routine: $routineText, Time: $timeText")
                        val currentRoutineText = routineText // 값을 별도의 변수에 저장
                        val currentTimeText = timeText     // 값을 별도의 변수에 저장

                        val timeParts = currentTimeText.split(":")
                        if (timeParts.size == 2) {
                            try {
                                val hour = timeParts[0].toInt()
                                val minute = timeParts[1].toInt()
                                val calendar = Calendar.getInstance()
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                calendar.set(Calendar.SECOND, 0)
                                calendar.set(Calendar.MILLISECOND, 0)

                                val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
                                val nowMinute = calendar.get(Calendar.MINUTE)

                                // If the set time is in the past, set it for the next day
                                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                                }

                                coroutineScope.launch {
                                    val newRoutine = Routine(name = currentRoutineText, time = currentTimeText)
                                    val routineId = routineDao.insertRoutine(newRoutine).toInt()
                                    Log.d("RoutineDebug", "Routine saved to DB with ID: $routineId, Name: ${newRoutine.name}, Time: ${newRoutine.time}")

                                    val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                                        putExtra("routineName", currentRoutineText)
                                        putExtra("routineId", routineId)
                                    }

                                    val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>() // This part seems left over from WorkManager, should be removed.
                                        // .setInputData(inputData) // This part seems left over from WorkManager, should be removed.
                                        // .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS) // This part seems left over from WorkManager, should be removed.
                                        .addTag(routineId.toString())
                                        .build()

                                    val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                                        context,
                                        routineId, // Use routineId as request code
                                        alarmIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )

                                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                                }

                                routineText = ""
                                timeText = ""
                            } catch (e: NumberFormatException) {
                                Log.e("RoutineDebug", "Invalid time format: $timeText", e)
                            }
                        } else {
                            Log.e("RoutineDebug", "Invalid time format: $timeText")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("루틴 추가")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 여기 있던 초기화 버튼 코드는 삭제하고 topBar로 옮겼습니다.
            // Button(...)
            // Spacer(...)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(routines) { routine ->
                    val calendar = Calendar.getInstance()
                    // Set time to the beginning of the current day
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startOfDayMillis = calendar.timeInMillis

                    // Set time to the end of the current day
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val endOfDayMillis = calendar.timeInMillis

                    val isCompleted by routineDao.isRoutineCompletedOnDate(routine.id, startOfDayMillis, endOfDayMillis).collectAsState(initial = 0)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(color = if (isCompleted > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) // Change background based on completion
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("루틴: ${routine.name}")
                            Text("시간: ${routine.time}" + if (isCompleted > 0) " (완료)" else "") // Add "(완료)" text
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RoutineScreenPreview() {
    RoutineTheme {
        // RoutineListScreen(routineDao = FakeRoutineDao(), onNavigateToCalendar = {})
    }
}
