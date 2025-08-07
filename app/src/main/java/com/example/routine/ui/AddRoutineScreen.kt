package com.example.routine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routine.data.Routine
import kotlinx.coroutines.launch

@Composable
fun AddRoutineScreen(viewModel: RoutineViewModel) {
    var routineContent by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

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

        Text(
            "Selected Time: ${String.format("%02d", selectedHour)}:${String.format("%02d", selectedMinute)}",
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HourPicker(selectedHour = selectedHour) { hour -> selectedHour = hour }
            MinutePicker(selectedMinute = selectedMinute) { minute -> selectedMinute = minute }
        }

        Button(onClick = {
            coroutineScope.launch {
                viewModel.insertRoutine(
                    Routine(content = routineContent, hour = selectedHour, minute = selectedMinute)
                )
                routineContent = ""
                selectedHour = 0
                selectedMinute = 0
            }
        }) {
            Text("Save Routine")
        }
    }
}

@Composable
fun HourPicker(selectedHour: Int, onHourSelected: (Int) -> Unit) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedHour) {
        listState.animateScrollToItem(selectedHour)
    }

    LazyColumn(
        modifier = Modifier
            .height(150.dp)
            .width(60.dp),
        state = listState
    ) {
        items((0..23).toList()) { hour ->
            Text(
                text = String.format("%02d", hour),
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHourSelected(hour) }
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MinutePicker(selectedMinute: Int, onMinuteSelected: (Int) -> Unit) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedMinute) {
        listState.animateScrollToItem(selectedMinute)
    }

    LazyColumn(
        modifier = Modifier
            .height(150.dp)
            .width(60.dp),
        state = listState
    ) {
        items((0..59).toList()) { minute ->
            Text(
                text = String.format("%02d", minute),
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMinuteSelected(minute) }
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
