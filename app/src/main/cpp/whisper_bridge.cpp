#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

#define LOG_TAG "WhisperBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifdef WHISPER_AVAILABLE
#include "whisper.h"
static struct whisper_context* g_ctx = nullptr;
#else
static void* g_ctx = nullptr;
#endif

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_lemonsubtitle_WhisperBridge_nativeInit(
    JNIEnv* env,
    jobject /* this */,
    jstring model_path
) {
    const char* path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("Initializing whisper with model: %s", path);

#ifdef WHISPER_AVAILABLE
    if (g_ctx) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
    }
    g_ctx = whisper_init_from_file(path);
    if (!g_ctx) {
        LOGE("whisper_init_from_file failed");
        env->ReleaseStringUTFChars(model_path, path);
        return env->NewStringUTF("Model init failed: unable to load GGUF file");
    }
    LOGI("Whisper model loaded successfully");
#else
    LOGE("Whisper not available at compile time");
#endif

    env->ReleaseStringUTFChars(model_path, path);
    return env->NewStringUTF("initialized");
}

JNIEXPORT jstring JNICALL
Java_com_lemonsubtitle_WhisperBridge_nativeTranscribe(
    JNIEnv* env,
    jobject /* this */,
    jstring audio_path,
    jboolean auto_detect_lang
) {
    const char* audio = env->GetStringUTFChars(audio_path, nullptr);
    LOGI("Transcribing audio: %s", audio);

#ifdef WHISPER_AVAILABLE
    if (!g_ctx) {
        env->ReleaseStringUTFChars(audio_path, audio);
        return env->NewStringUTF("{\"error\": \"Model not initialized\"}");
    }

    // Read WAV file
    FILE* f = fopen(audio, "rb");
    if (!f) {
        LOGE("Cannot open audio file: %s", audio);
        env->ReleaseStringUTFChars(audio_path, audio);
        return env->NewStringUTF("{\"error\": \"Cannot open audio file\"}");
    }

    fseek(f, 0, SEEK_END);
    long fsize = ftell(f);
    fseek(f, 0, SEEK_SET);

    if (fsize < 44) {
        fclose(f);
        env->ReleaseStringUTFChars(audio_path, audio);
        return env->NewStringUTF("{\"error\": \"Invalid WAV file\"}");
    }

    // Skip WAV header (44 bytes)
    fseek(f, 44, SEEK_SET);
    int16_t* pcm16 = new int16_t[(fsize - 44) / 2];
    size_t samples_read = fread(pcm16, 2, (fsize - 44) / 2, f);
    fclose(f);

    // Convert int16 to float (whisper expects float32)
    std::vector<float> pcmf32(samples_read);
    for (size_t i = 0; i < samples_read; i++) {
        pcmf32[i] = pcm16[i] / 32768.0f;
    }
    delete[] pcm16;

    // Run whisper
    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_progress = false;
    params.print_realtime = false;
    params.print_timestamps = false;
    params.n_threads = 4;
    params.speed_up = false;
    params.thold_pt = 0.01f;

    if (auto_detect_lang) {
        params.language = nullptr;
    } else {
        params.language = "en";
    }

    if (whisper_full(g_ctx, params, pcmf32.data(), (int)pcmf32.size()) != 0) {
        LOGE("whisper_full failed");
        env->ReleaseStringUTFChars(audio_path, audio);
        return env->NewStringUTF("{\"error\": \"Transcription failed\"}");
    }

    // Build JSON result
    std::string result = "{\"segments\":[";
    int n_segments = whisper_full_n_segments(g_ctx);
    for (int i = 0; i < n_segments; i++) {
        if (i > 0) result += ",";
        const char* text = whisper_full_get_segment_text(g_ctx, i);
        int64_t t0 = whisper_full_get_segment_t0(g_ctx, i);
        int64_t t1 = whisper_full_get_segment_t1(g_ctx, i);

        // JSON-escape the text
        std::string escaped;
        for (const char* p = text; *p; p++) {
            switch (*p) {
                case '"': escaped += "\\\""; break;
                case '\\': escaped += "\\\\"; break;
                case '\n': escaped += "\\n"; break;
                case '\r': escaped += "\\r"; break;
                case '\t': escaped += "\\t"; break;
                default: escaped += *p;
            }
        }

        result += "{\"start\":" + std::to_string(t0) +
                  ",\"end\":" + std::to_string(t1) +
                  ",\"text\":\"" + escaped + "\"}";
    }
    result += "],\"language\":\"" +
              std::string(whisper_lang_str(whisper_full_lang_id(g_ctx))) + "\"}";

    LOGI("Transcription complete: %d segments", n_segments);
    env->ReleaseStringUTFChars(audio_path, audio);
    return env->NewStringUTF(result.c_str());
#else
    env->ReleaseStringUTFChars(audio_path, audio);
    return env->NewStringUTF("{\"error\": \"Whisper not available\"}");
#endif
}

JNIEXPORT void JNICALL
Java_com_lemonsubtitle_WhisperBridge_nativeRelease(
    JNIEnv* env,
    jobject /* this */
) {
    LOGI("Releasing whisper resources");
#ifdef WHISPER_AVAILABLE
    if (g_ctx) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
    }
#endif
}

} // extern "C"
