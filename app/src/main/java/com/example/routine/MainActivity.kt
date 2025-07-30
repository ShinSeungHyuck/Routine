package com.example.routine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.routine.ui.theme.RoutineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoutineTheme {
                RoutineScreen()
            }
        }
    }
}

@Composable
fun RoutineScreen() {
    var routineText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    val routines = remember { mutableStateListOf<Pair<String, String>>() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                label = { Text("시간을 입력하세요") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (routineText.isNotBlank() && timeText.isNotBlank()) {
                        routines.add(Pair(routineText, timeText))
                        routineText = ""
                        timeText = ""
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
                            Text("루틴: ${routine.first}")
                            Text("시간: ${routine.second}")
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
        RoutineScreen()
    }
}
