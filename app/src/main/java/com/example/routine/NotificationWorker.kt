package com.example.routine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.routine.data.RoutineCompletion
import com.example.routine.data.RoutineDatabase
import java.util.Date

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val routineName = inputData.getString("routineName") ?: "Your Routine"
        val routineId = inputData.getInt("routineId", -1)

        if (routineId != -1) {
            // Save routine completion to database
            val db = RoutineDatabase.getDatabase(applicationContext)
            val routineDao = db.routineDao()
            val completion = RoutineCompletion(routineId = routineId, completionDate = Date().time)
            routineDao.insertRoutineCompletion(completion)
        }

        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // You might want to add extra data here to identify the routine
            putExtra("routineName", routineName)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "routine_channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
            .setContentTitle("Routine Reminder")
            .setContentText("Time for your routine: $routineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(routineId, notification) // Use routineId as notification ID

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Routine Channel"
            val descriptionText = "Channel for routine reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("routine_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager:
                    NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}