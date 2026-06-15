package com.lemonsubtitle.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

enum class DownloadStatus {
    IDLE, DOWNLOADING, VALIDATING, COMPLETED, FAILED, CANCELLED
}

data class DownloadProgress(
    val modelId: String,
    val status: DownloadStatus,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val error: String = ""
)

data class ModelInfo(
    val id: String,
    val name: String,
    val fileName: String,
    val url: String,
    val sizeMb: String,
    val description: String
)

object ModelDownloadManager {

    private const val TAG = "ModelDownloadManager"
    private const val GGUF_MAGIC = 0x46475547L // "GGUF" in ASCII

    private val _progress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val progress: StateFlow<Map<String, DownloadProgress>> = _progress

    private val activeConnections = mutableMapOf<String, HttpURLConnection>()

    val availableModels = listOf(
        ModelInfo(
            id = "tiny",
            name = "Whisper tiny",
            fileName = "ggml-tiny.gguf",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.gguf",
            sizeMb = "75MB",
            description = "极速识别，低准确率"
        ),
        ModelInfo(
            id = "base",
            name = "Whisper base",
            fileName = "ggml-base.gguf",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.gguf",
            sizeMb = "142MB",
            description = "均衡速度与效果"
        ),
        ModelInfo(
            id = "small",
            name = "Whisper small",
            fileName = "ggml-small.gguf",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.gguf",
            sizeMb = "466MB",
            description = "高准确率，推荐使用"
        ),
        ModelInfo(
            id = "medium",
            name = "Whisper medium",
            fileName = "ggml-medium.gguf",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.gguf",
            sizeMb = "1.5GB",
            description = "更高准确率，需要更多内存"
        ),
        ModelInfo(
            id = "large-v3",
            name = "Whisper large-v3",
            fileName = "ggml-large-v3.gguf",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.gguf",
            sizeMb = "3.1GB",
            description = "最高准确率，速度较慢"
        )
    )

    suspend fun downloadModel(
        context: Context,
        model: ModelInfo,
        onProgress: (DownloadProgress) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            updateProgress(model.id, DownloadProgress(model.id, DownloadStatus.DOWNLOADING, 0f))
            onProgress(DownloadProgress(model.id, DownloadStatus.DOWNLOADING, 0f))

            val modelsDir = File(context.filesDir, "models").also { it.mkdirs() }
            val tempFile = File(modelsDir, "${model.fileName}.tmp")
            val targetFile = File(modelsDir, model.fileName)

            if (targetFile.exists()) {
                updateProgress(model.id, DownloadProgress(model.id, DownloadStatus.COMPLETED))
                return@withContext Result.success(targetFile.absolutePath)
            }

            val connection = URL(model.url).openConnection() as HttpURLConnection
            activeConnections[model.id] = connection

            try {
                connection.connectTimeout = 30_000
                connection.readTimeout = 60_000
                connection.connect()

                if (connection.responseCode != 200) {
                    throw RuntimeException("HTTP ${connection.responseCode}: ${connection.responseMessage}")
                }

                val totalBytes = connection.contentLength.toLong()
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(tempFile)

                val buffer = ByteArray(8192)
                var downloadedBytes = 0L
                var lastProgressUpdate = 0L

                try {
                    while (true) {
                        val bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) break

                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastProgressUpdate > 500 || bytesRead == -1) {
                            val progress = if (totalBytes > 0) {
                                downloadedBytes.toFloat() / totalBytes
                            } else {
                                -1f
                            }
                            val progressData = DownloadProgress(
                                modelId = model.id,
                                status = DownloadStatus.DOWNLOADING,
                                progress = progress,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes
                            )
                            updateProgress(model.id, progressData)
                            onProgress(progressData)
                            lastProgressUpdate = currentTime
                        }
                    }
                } finally {
                    outputStream.close()
                    inputStream.close()
                }

                if (activeConnections[model.id]?.isCanceled == true) {
                    tempFile.delete()
                    updateProgress(model.id, DownloadProgress(model.id, DownloadStatus.CANCELLED))
                    return@withContext Result.failure(Exception("Download cancelled"))
                }

                updateProgress(model.id, DownloadProgress(model.id, DownloadStatus.VALIDATING, 1f))
                onProgress(DownloadProgress(model.id, DownloadStatus.VALIDATING, 1f))

                if (!validateGgufFile(tempFile)) {
                    tempFile.delete()
                    val error = "Invalid GGUF file: file may be corrupted or not a valid model"
                    updateProgress(model.id, DownloadProgress(model.id, DownloadStatus.FAILED, error = error))
                    return@withContext Result.failure(Exception(error))
                }

                tempFile.renameTo(targetFile)
                updateProgress(model.id, DownloadProgress(model.id, DownloadStatus.COMPLETED, 1f))
                onProgress(DownloadProgress(model.id, DownloadStatus.COMPLETED, 1f))

                Log.i(TAG, "Model downloaded and validated: ${model.name}")
                Result.success(targetFile.absolutePath)
            } catch (e: Exception) {
                tempFile.delete()
                val error = e.message ?: "Download failed"
                updateProgress(model.id, DownloadProgress(model.id, DownloadStatus.FAILED, error = error))
                onProgress(DownloadProgress(model.id, DownloadStatus.FAILED, error = error))
                Log.e(TAG, "Download failed: $error", e)
                Result.failure(e)
            } finally {
                connection.disconnect()
                activeConnections.remove(model.id)
            }
        } catch (e: Exception) {
            val error = e.message ?: "Unknown error"
            updateProgress(model.id, DownloadProgress(model.id, DownloadStatus.FAILED, error = error))
            onProgress(DownloadProgress(model.id, DownloadStatus.FAILED, error = error))
            Result.failure(e)
        }
    }

    fun cancelDownload(modelId: String) {
        activeConnections[modelId]?.cancel()
        activeConnections.remove(modelId)
        updateProgress(modelId, DownloadProgress(modelId, DownloadStatus.CANCELLED))
    }

    private fun validateGgufFile(file: File): Boolean {
        try {
            if (!file.exists() || file.length() < 4) {
                return false
            }

            val inputStream = file.inputStream()
            val header = ByteArray(4)
            val bytesRead = inputStream.read(header)
            inputStream.close()

            if (bytesRead < 4) {
                return false
            }

            val magic = (header[0].toLong() and 0xFF) or
                    ((header[1].toLong() and 0xFF) shl 8) or
                    ((header[2].toLong() and 0xFF) shl 16) or
                    ((header[3].toLong() and 0xFF) shl 24)

            val isValid = magic == GGUF_MAGIC
            Log.d(TAG, "GGUF validation: magic=0x${magic.toString(16)}, valid=$isValid")
            return isValid
        } catch (e: Exception) {
            Log.e(TAG, "Validation failed", e)
            return false
        }
    }

    private fun updateProgress(modelId: String, progress: DownloadProgress) {
        _progress.value = _progress.value.toMutableMap().apply {
            put(modelId, progress)
        }
    }

    fun resetProgress(modelId: String) {
        _progress.value = _progress.value.toMutableMap().apply {
            remove(modelId)
        }
    }
}
