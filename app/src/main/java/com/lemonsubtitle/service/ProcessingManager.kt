package com.lemonsubtitle.service

import android.content.Context
import android.net.Uri
import com.lemonsubtitle.FFmpegBridge
import com.lemonsubtitle.WhisperBridge
import com.lemonsubtitle.model.SubtitleLine
import com.lemonsubtitle.model.SubtitleParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class ProcessingStep {
    EXTRACT_AUDIO, TRANSCRIBE, PARSE, TRANSLATE, COMPLETE, FAILED
}

data class ProcessingProgress(
    val taskId: String,
    val fileName: String,
    val step: ProcessingStep,
    val progress: Float,
    val message: String
)

data class ProcessingResult(
    val success: Boolean,
    val subtitles: List<SubtitleLine> = emptyList(),
    val error: String = ""
)

object ProcessingManager {

    suspend fun processFile(
        context: Context,
        fileUri: Uri,
        fileName: String,
        fileType: String,
        onProgress: (ProcessingProgress) -> Unit
    ): ProcessingResult = withContext(Dispatchers.Default) {
        val taskId = UUID.randomUUID().toString()
        val cacheDir = context.cacheDir
        val wavFile = File(cacheDir, "${taskId}_temp.wav")

        try {
            // Step 1: Extract audio (if video/audio)
            var audioPath: String = fileUri.toString()
            if (fileType == "video") {
                onProgress(ProcessingProgress(taskId, fileName, ProcessingStep.EXTRACT_AUDIO, 0f, "提取音频中..."))
                FFmpegBridge.extractAudio(fileUri.toString(), wavFile.absolutePath)
                    .getOrThrow()
                audioPath = wavFile.absolutePath
            } else if (fileType == "audio") {
                // Copy audio file to cache for whisper
                onProgress(ProcessingProgress(taskId, fileName, ProcessingStep.EXTRACT_AUDIO, 0f, "准备音频文件..."))
                context.contentResolver.openInputStream(fileUri)?.use { input ->
                    wavFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                audioPath = wavFile.absolutePath
            }

            // Step 2: Transcribe with Whisper
            onProgress(ProcessingProgress(taskId, fileName, ProcessingStep.TRANSCRIBE, 0.3f, "转写中..."))
            val transcriptionJson = WhisperBridge.transcribe(audioPath, autoDetectLang = true)
                .getOrThrow()

            // Step 3: Parse transcription result
            onProgress(ProcessingProgress(taskId, fileName, ProcessingStep.PARSE, 0.7f, "解析结果..."))
            val json = JSONObject(transcriptionJson)
            val segments = json.optJSONArray("segments")
            val detectedLang = json.optString("language", "en")

            if (segments == null || segments.length() == 0) {
                return@withContext ProcessingResult(false, error = "No speech detected")
            }

            val subtitles = mutableListOf<SubtitleLine>()
            for (i in 0 until segments.length()) {
                val seg = segments.getJSONObject(i)
                subtitles.add(
                    SubtitleLine(
                        startMs = seg.getLong("start") / 10000,
                        endMs = seg.getLong("end") / 10000,
                        text = seg.getString("text").trim()
                    )
                )
            }

            // Step 4: Translate (marked complete - actual translation triggers on demand in editor)
            onProgress(ProcessingProgress(taskId, fileName, ProcessingStep.COMPLETE, 1f, "处理完成"))

            ProcessingResult(success = true, subtitles = subtitles)
        } catch (e: Exception) {
            onProgress(ProcessingProgress(taskId, fileName, ProcessingStep.FAILED, 0f, e.message ?: "处理失败"))
            ProcessingResult(success = false, error = e.message ?: "Unknown error")
        } finally {
            if (wavFile.exists()) wavFile.delete()
        }
    }

    fun isAudio(fileName: String): Boolean {
        val name = fileName.lowercase()
        return name.endsWith(".mp3") || name.endsWith(".wav") ||
               name.endsWith(".m4a") || name.endsWith(".ogg") ||
               name.endsWith(".aac") || name.endsWith(".flac")
    }

    fun isVideo(fileName: String): Boolean {
        val name = fileName.lowercase()
        return name.endsWith(".mp4") || name.endsWith(".mkv") ||
               name.endsWith(".mov") || name.endsWith(".avi") ||
               name.endsWith(".webm") || name.endsWith(".m4a")
    }

    fun isSubtitle(fileName: String): Boolean {
        val name = fileName.lowercase()
        return name.endsWith(".srt") || name.endsWith(".vtt")
    }
}
