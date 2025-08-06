package com.example.routine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val routineName = intent.getStringExtra("routineName") ?: "Your Routine"
        val routineId = intent.getIntExtra("routineId", -1)

        createNotificationChannel(context)

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("routineName", routineName)
            putExtra("routineId", routineId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            routineId, // Use routineId for request code to make them unique
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "routine_channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
            .setContentTitle("Routine Reminder")
            .setContentText("Time for your routine: $routineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (routineId != -1) {
            notificationManager.notify(routineId, notification) // Use routineId as notification ID
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Routine Channel"
            val descriptionText = "Channel for routine reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("routine_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}