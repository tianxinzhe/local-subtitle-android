package com.lemonsubtitle.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_DEFAULT_LANG = stringPreferencesKey("default_target_lang")
        private val KEY_PRECISION_MODE = intPreferencesKey("precision_mode")
        private val KEY_OUTPUT_DIR = stringPreferencesKey("output_dir")
        private val KEY_SOURCE_LANG = stringPreferencesKey("source_lang")
    }

    val defaultTargetLanguage: Flow<String> = context.dataStore.data.map {
        it[KEY_DEFAULT_LANG] ?: "zh"
    }

    val precisionMode: Flow<Int> = context.dataStore.data.map {
        it[KEY_PRECISION_MODE] ?: 1
    }

    val outputDirectory: Flow<String> = context.dataStore.data.map {
        it[KEY_OUTPUT_DIR] ?: ""
    }

    suspend fun setDefaultTargetLanguage(lang: String) {
        context.dataStore.edit { it[KEY_DEFAULT_LANG] = lang }
    }

    suspend fun setPrecisionMode(mode: Int) {
        context.dataStore.edit { it[KEY_PRECISION_MODE] = mode }
    }

    suspend fun setOutputDirectory(dir: String) {
        context.dataStore.edit { it[KEY_OUTPUT_DIR] = dir }
    }
}
