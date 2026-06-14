package com.lemonsubtitle.service

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object TranslationHelper {

    private val translators = mutableMapOf<String, Translator>()

    suspend fun translate(
        text: String,
        sourceLang: String = TranslateLanguage.ENGLISH,
        targetLang: String = TranslateLanguage.CHINESE
    ): String {
        val key = "$sourceLang-$targetLang"
        val translator = translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            Translation.getClient(options)
        }

        suspendCancellableCoroutine { continuation ->
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    translator.translate(text)
                        .addOnSuccessListener { result ->
                            continuation.resume(result)
                        }
                        .addOnFailureListener { e ->
                            continuation.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    suspend fun translate(
        texts: List<String>,
        sourceLang: String = TranslateLanguage.ENGLISH,
        targetLang: String = TranslateLanguage.CHINESE
    ): List<String> {
        return texts.map { translate(it, sourceLang, targetLang) }
    }

    fun releaseAll() {
        translators.values.forEach { it.close() }
        translators.clear()
    }

    fun release(sourceLang: String, targetLang: String) {
        val key = "$sourceLang-$targetLang"
        translators.remove(key)?.close()
    }
}
