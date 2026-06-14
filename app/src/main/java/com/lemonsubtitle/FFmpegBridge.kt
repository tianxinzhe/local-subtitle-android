package com.lemonsubtitle

object FFmpegBridge {
    init {
        System.loadLibrary("lemonsubtitle")
    }

    external fun nativeExtractAudio(inputPath: String, outputPath: String): String

    fun extractAudio(inputPath: String, outputPath: String): Result<Unit> = runCatching {
        val result = nativeExtractAudio(inputPath, outputPath)
        if (result.isNotEmpty()) throw RuntimeException("FFmpeg error: $result")
    }
}
