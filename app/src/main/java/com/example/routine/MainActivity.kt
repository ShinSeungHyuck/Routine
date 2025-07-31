package com.example.routine

import android.os.Bundle
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
import android.util.Log // Import Log
// import androidx.compose.material.icons.filled.Delete // Import Delete Icon
import androidx.compose.material.icons.filled.DateRange // Re-import DateRange for clarity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            Row( // Use Row to place buttons side-by-side
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // Distribute space evenly
            ) {
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
                                    val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
                                    val nowMinute = calendar.get(Calendar.MINUTE)

                                    var delayInMinutes = (hour * 60 + minute) - (nowHour * 60 + nowMinute)
                                    if (delayInMinutes < 0) {
                                        delayInMinutes += 24 * 60
                                    }

                                    val delayInMillis = delayInMinutes * 60 * 1000L

                                    coroutineScope.launch {
                                        val newRoutine = Routine(name = currentRoutineText, time = currentTimeText)
                                        val routineId = routineDao.insertRoutine(newRoutine).toInt()
                                        Log.d("RoutineDebug", "Routine saved to DB with ID: $routineId, Name: ${newRoutine.name}, Time: ${newRoutine.time}")

                                        val inputData = Data.Builder()
                                            .putString("routineName", currentRoutineText)
                                            .putInt("routineId", routineId)
                                            .build()

                                        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                                            .setInputData(inputData)
                                            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                                            .addTag(routineId.toString())
                                            .build()

                                        WorkManager.getInstance(context).enqueue(notificationWorkRequest)
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
                    modifier = Modifier.weight(1f) // occupy half the width
                ) {
                    Text("루틴 추가")
                }
                Spacer(modifier = Modifier.width(8.dp)) // Add a spacer for separation
                Button(
                    onClick = {
                        coroutineScope.launch {
                            routineDao.deleteAllRoutines()
                            Log.d("RoutineDebug", "All routines deleted from delete button.") // 삭제 확인 로그
                        }
                    },
                    modifier = Modifier.weight(1f) // occupy half the width
                ) {
                    Text("루틴 삭제")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 여기 있던 초기화 버튼 코드는 삭제하고 topBar로 옮겼습니다.
            // Button(...)
            // Spacer(...)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(routines) {
                    routine ->
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


@Preview(showBackground = true)
@Composable
fun RoutineScreenPreview() {
    RoutineTheme {
        // RoutineListScreen(routineDao = FakeRoutineDao(), onNavigateToCalendar = {})
    }
}
