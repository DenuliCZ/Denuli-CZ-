package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExportForegroundService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        const val CHANNEL_ID = "export_channel"
        const val NOTIFICATION_ID = 9001
        
        const val EXTRA_FORMAT = "format"
        const val EXTRA_DURATION_SEC = "duration_sec"
        
        const val ACTION_PROGRESS_UPDATE = "com.example.service.PROGRESS_UPDATE"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_ESTIMATED_LEFT = "estimated_left"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val format = intent?.getStringExtra(EXTRA_FORMAT) ?: "MP3"
        val duration = intent?.getIntExtra(EXTRA_DURATION_SEC, 10) ?: 10

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(0, duration, format),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification(0, duration, format))
        }

        serviceScope.launch {
            for (progress in 1..10) {
                delay(1000)
                val percentPercent = progress * 10
                val percentFloat = percentPercent / 100f
                val secondsLeft = (duration - (duration * percentFloat).toInt()).coerceAtLeast(1)

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, buildNotification(percentPercent, secondsLeft, format))

                val broadcastIntent = Intent(ACTION_PROGRESS_UPDATE).apply {
                    putExtra(EXTRA_PROGRESS, percentFloat)
                    putExtra(EXTRA_ESTIMATED_LEFT, secondsLeft)
                }
                sendBroadcast(broadcastIntent)
            }
            delay(500)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun buildNotification(progressPercent: Int, secondsLeft: Int, format: String): Notification {
        val message = "Exporting project to $format... $progressPercent% ($secondsLeft seconds left)"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Studio Denuli Model Render")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progressPercent, false)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Project Rendering Service"
            val descriptionText = "Notifications regarding high-end export renderings in Studio Denuli"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
