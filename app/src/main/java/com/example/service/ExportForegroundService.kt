package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.util.ExportFileHelper
import kotlinx.coroutines.*
import java.io.File

class ExportForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val format = intent?.getStringExtra(EXTRA_FORMAT) ?: "WAV"
        val duration = intent?.getIntExtra(EXTRA_DURATION_SEC, 15) ?: 15
        val lyrics = intent?.getStringExtra(EXTRA_LYRICS) ?: "Spark Studio composition"
        val genre = intent?.getStringExtra(EXTRA_GENRE) ?: "Pop"

        Log.d(TAG, "Starting export service: Format=$format, Duration=$duration, Genre=$genre")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(0, duration, format),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification(0, duration, format))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed starting service as foreground: ${e.message}", e)
        }

        serviceScope.launch {
            try {
                if (format == "MP4") {
                    updateNotification(10, duration, format)
                    val soundFile = ExportFileHelper.generateRealAudioTrack(this@ExportForegroundService, genre, duration) { progress ->
                        val subProgress = (progress * 50).toInt() + 10
                        updateNotification(subProgress, duration, format)
                    }
                    updateNotification(60, duration, format)
                    ExportFileHelper.generateRealLyricVideo(this@ExportForegroundService, lyrics, genre, soundFile, duration) { progress ->
                        val subProgress = (progress * 40).toInt() + 60
                        updateNotification(subProgress, duration, format)
                    }
                } else {
                    updateNotification(10, duration, format)
                    ExportFileHelper.generateRealAudioTrack(this@ExportForegroundService, genre, duration) { progress ->
                        val subProgress = (progress * 90).toInt() + 10
                        updateNotification(subProgress, duration, format)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Export failure in foreground service: ${e.message}", e)
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun buildNotification(progress: Int, duration: Int, format: String): Notification {
        val pct = progress.coerceIn(0, 100)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Spark Studio: Exportování")
            .setContentText("Generuji skladbu do formátu $format ($pct%)")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, pct, false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(progress: Int, duration: Int, format: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(progress, duration, format))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Spark Studio Export Services",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Upozornění o průběhu exportu a tvorby videí."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "ExportForegroundService"
        private const val CHANNEL_ID = "spark_studio_export_chan_v2"
        private const val NOTIFICATION_ID = 241105

        const val EXTRA_FORMAT = "extra_format"
        const val EXTRA_DURATION_SEC = "extra_duration_sec"
        const val EXTRA_LYRICS = "extra_lyrics"
        const val EXTRA_GENRE = "extra_genre"
    }
}
