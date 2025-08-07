package com.example.routine

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routine.data.RoutineDatabase
import com.example.routine.ui.RoutineViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var routineViewModel: RoutineViewModel
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                scheduleNotification()
            } else {
                // 권한 거부 처리
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val database = RoutineDatabase.getDatabase(applicationContext)
        val routineDao = database.routineDao()
        routineViewModel = RoutineViewModel(routineDao)

        checkNotificationPermission()

        lifecycleScope.launch {
            routineViewModel.allRoutines.collect { routines ->
                scheduleRoutineNotifications(routines)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // 이미 권한 있음
                scheduleNotification()
            }
        } else {
            // Android 12 이하
            scheduleNotification()
        }
    }

    private fun scheduleRoutineNotifications(routines: List<com.example.routine.data.Routine>) {
        val workManager = WorkManager.getInstance(this)
        // Cancel any existing scheduled work to avoid duplicates
        workManager.cancelAllWorkByTag("routine_notification")

        if (routines.isEmpty()) {
            // No routines to schedule notifications for
            return
        }

        routines.forEach { routine ->
            val currentTime = Calendar.getInstance()
            val dueTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, routine.hour)
                set(Calendar.MINUTE, routine.minute)
                set(Calendar.SECOND, 0)
                // If the scheduled time is in the past, schedule for the next day
                if (before(currentTime)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val delay = dueTime.timeInMillis - currentTime.timeInMillis

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(androidx.work.Data.Builder().putString("routineContent", routine.content).build())
                .addTag("routine_notification")
                .build()
            workManager.enqueueUniqueWork(routine.id.toString(), ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}
