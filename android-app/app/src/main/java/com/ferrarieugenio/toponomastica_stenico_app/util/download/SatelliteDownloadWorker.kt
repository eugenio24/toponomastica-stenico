package com.ferrarieugenio.toponomastica_stenico_app.util.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SatelliteDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "satellite_download"

    override suspend fun doWork(): Result = coroutineScope {
        val maxRetries = 3
        val attempt = runAttemptCount

        if (attempt > maxRetries) {
            return@coroutineScope Result.failure(workDataOf("error" to "Max retry attempts reached"))
        }

        val manager = SatelliteDataManager(applicationContext)
        val progressChannel = Channel<Int>(Channel.UNLIMITED)

        createNotificationChannel()

        val startTime = System.currentTimeMillis()

        setForegroundAsync(createForegroundInfo("Download inizializzato", 0))

        val progressJob = launch {
            for (progress in progressChannel) {
                setProgress(workDataOf(
                    "progress" to progress,
                    "downloadStartTime" to startTime
                ))
                setForegroundAsync(createForegroundInfo("Download dati satellitari", progress))
            }
        }

        return@coroutineScope try {
            manager.downloadAndExtractSatelliteData(progressChannel)
            progressJob.join()
            Result.success()
        } catch (e: Exception) {
            progressJob.cancel()
            Result.retry()
        }
    }

    private fun createForegroundInfo(title: String, progress: Int): ForegroundInfo {
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$progress% completato")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification, foregroundServiceType)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Download dati satellitari"
            val descriptionText = "Notifiche per il download della mappa satellitare"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}