package com.example.miniprojectmap

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Membuat file penyimpanan bernama "settings"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    // Kunci untuk menyimpan status Dark Mode (True/False)
    private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")

    // 1. Membaca Data (Flow: Mengalir terus jika ada perubahan)
    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // Default-nya False (Light Mode) jika belum ada settingan
            preferences[IS_DARK_MODE_KEY] ?: false
        }

    // 2. Menyimpan Data
    suspend fun saveThemeSetting(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark
        }
    }
}