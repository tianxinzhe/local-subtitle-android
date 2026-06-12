#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "LemonSubtitle"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_lemonsubtitle_WhisperBridge_nativeInit(
    JNIEnv* env,
    jobject /* this */,
    jstring model_path
) {
    const char* path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("Initializing whisper with model: %s", path);
    // TODO: whisper.cpp init
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
    // TODO: whisper.cpp transcribe
    env->ReleaseStringUTFChars(audio_path, audio);
    return env->NewStringUTF("{\"text\": \"transcription result\"}");
}

JNIEXPORT void JNICALL
Java_com_lemonsubtitle_WhisperBridge_nativeRelease(
    JNIEnv* env,
    jobject /* this */
) {
    LOGI("Releasing whisper resources");
    // TODO: cleanup
}

} // extern "C"
