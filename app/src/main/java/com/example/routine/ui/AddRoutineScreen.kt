package com.example.routine.ui

import androidx.compose.animation.core.snap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routine.data.Routine
import kotlinx.coroutines.launch

@Composable
fun AddRoutineScreen(viewModel: RoutineViewModel) {
    var routineContent by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {
        OutlinedTextField(
            value = routineContent,
            onValueChange = { routineContent = it },
            label = { Text("Routine Content") },
            modifier = Modifier.fillMaxWidth()
        )
        
        LaunchedEffect(selectedHour) { listState.animateScrollToItem(selectedHour) }
    }
}


        Text("Selected Time: $selectedHour:$selectedMinute") // Placeholder for time display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HourPicker(selectedHour = selectedHour) { hour ->
                selectedHour = hour
            }
            MinutePicker(selectedMinute = selectedMinute) { minute ->
                selectedMinute = minute
            }
        }

        val coroutineScope = rememberCoroutineScope()
        Button(onClick = {
            coroutineScope.launch {
                viewModel.insertRoutine(Routine(content = routineContent, hour = selectedHour, minute = selectedMinute))
            }
        }) {
            Text("Save Routine")
        }
    }
}

@Composable
fun HourPicker(selectedHour: Int, onHourSelected: (Int) -> Unit) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.height(150.dp).width(60.dp), state = listState) {
        items((0..23).toList()) { hour ->
            Text(
                text = String.format("%02d", hour),
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHourSelected(hour) }
                    .padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MinutePicker(selectedMinute: Int, onMinuteSelected: (Int) -> Unit) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.height(150.dp).width(60.dp), state = listState) {
        items((0..59).toList()) { minute ->
            Text(
                text = String.format("%02d", minute),
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMinuteSelected(minute) }
                    .padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

        LaunchedEffect(selectedMinute) { listState.animateScrollToItem(selectedMinute) }
    }
}