package com.example.routine

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.pm.PackageManager


class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val routineContent = inputData.getString("routineContent") ?: "루틴을 확인하세요!"

        val routineId = inputData.getInt("routineId", -1) // Get routine ID

        // 알람 권한 체크 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }

        showNotification(routineContent, routineId)
        return Result.success()
    }

    private fun showNotification(routineContent: String) {
        val channelId = "routine_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Routine Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("routineId", routineId) // Pass routine ID
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext,
            routineId, // Use routineId as request code for unique PendingIntent
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("루틴 알람")
            .setContentText(routineContent)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent) // Set the PendingIntent
            .build()

        notificationManager.notify(1, notification)
    }
}
