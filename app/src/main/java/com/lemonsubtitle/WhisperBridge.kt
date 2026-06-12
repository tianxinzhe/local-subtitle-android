package com.lemonsubtitle

/**
 * JNI bridge to whisper.cpp native library.
 */
object WhisperBridge {
    init {
        System.loadLibrary("lemonsubtitle")
    }

    external fun nativeInit(modelPath: String): String
    external fun nativeTranscribe(audioPath: String, autoDetectLang: Boolean): String
    external fun nativeRelease()

    fun initialize(modelPath: String): Result<Unit> = runCatching {
        val result = nativeInit(modelPath)
        if (result != "initialized") throw RuntimeException("Init failed: $result")
    }

    fun transcribe(audioPath: String, autoDetectLang: Boolean = true): Result<String> = runCatching {
        nativeTranscribe(audioPath, autoDetectLang)
    }

    fun release() {
        nativeRelease()
    }
}
