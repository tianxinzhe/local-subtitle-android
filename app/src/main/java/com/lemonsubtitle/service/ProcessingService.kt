package com.lemonsubtitle.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ProcessingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var processingJob: kotlinx.coroutines.Job? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("准备处理...", 0)
        startForeground(NOTIFICATION_ID, notification)

        val action = intent?.getStringExtra(EXTRA_ACTION)
        when (action) {
            ACTION_START_PROCESSING -> {
                val fileUri = intent.getStringExtra(EXTRA_FILE_URI) ?: return START_NOT_STICKY
                val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return START_NOT_STICKY
                val fileType = intent.getStringExtra(EXTRA_FILE_TYPE) ?: return START_NOT_STICKY
                startProcessing(fileUri, fileName, fileType)
            }
            ACTION_STOP -> {
                stopProcessing()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startProcessing(fileUri: String, fileName: String, fileType: String) {
        processingJob?.cancel()
        processingJob = serviceScope.launch {
            try {
                updateProgress("正在处理: $fileName", 0)
                val uri = android.net.Uri.parse(fileUri)
                val result = ProcessingManager.processFile(
                    context = applicationContext,
                    fileUri = uri,
                    fileName = fileName,
                    fileType = fileType,
                    onProgress = { progress ->
                        updateProgress(
                            "正在处理: $fileName",
                            (progress.progress * 100).toInt()
                        )
                    }
                )
                if (result.success) {
                    sendBroadcast(Intent(ACTION_PROCESSING_COMPLETE).apply {
                        putExtra(EXTRA_FILE_NAME, fileName)
                        putExtra(EXTRA_SUCCESS, true)
                    })
                    onComplete()
                } else {
                    sendBroadcast(Intent(ACTION_PROCESSING_COMPLETE).apply {
                        putExtra(EXTRA_FILE_NAME, fileName)
                        putExtra(EXTRA_SUCCESS, false)
                        putExtra(EXTRA_ERROR, result.error)
                    })
                    onError(result.error)
                }
            } catch (e: Exception) {
                sendBroadcast(Intent(ACTION_PROCESSING_COMPLETE).apply {
                    putExtra(EXTRA_FILE_NAME, fileName)
                    putExtra(EXTRA_SUCCESS, false)
                    putExtra(EXTRA_ERROR, e.message ?: "Unknown error")
                })
                onError(e.message ?: "Unknown error")
            }
        }
    }

    private fun stopProcessing() {
        processingJob?.cancel()
        processingJob = null
    }

    fun updateProgress(message: String, progress: Int) {
        val notification = createNotification(message, progress)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun onComplete() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LemonSubtitle")
            .setContentText("处理完成")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun onError(error: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LemonSubtitle")
            .setContentText("处理失败: $error")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Processing Tasks",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background subtitle processing"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String, progress: Int): Notification {
        val stopIntent = Intent(this, ProcessingService::class.java).apply {
            putExtra(EXTRA_ACTION, ACTION_STOP)
        }
        val stopPendingIntent = android.app.PendingIntent.getService(
            this, 0, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LemonSubtitle")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "取消", stopPendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        instance = null
    }

    companion object {
        private const val CHANNEL_ID = "subtitle_processing"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_ACTION = "action"
        const val EXTRA_FILE_URI = "file_uri"
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_FILE_TYPE = "file_type"
        const val EXTRA_SUCCESS = "success"
        const val EXTRA_ERROR = "error"

        const val ACTION_START_PROCESSING = "start_processing"
        const val ACTION_STOP = "stop"
        const val ACTION_PROCESSING_COMPLETE = "com.lemonsubtitle.PROCESSING_COMPLETE"

        var instance: ProcessingService? = null
            private set

        fun startProcessing(context: android.content.Context, fileUri: String, fileName: String, fileType: String) {
            val intent = Intent(context, ProcessingService::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_START_PROCESSING)
                putExtra(EXTRA_FILE_URI, fileUri)
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_FILE_TYPE, fileType)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: android.content.Context) {
            val intent = Intent(context, ProcessingService::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_STOP)
            }
            context.startService(intent)
        }
    }
}
