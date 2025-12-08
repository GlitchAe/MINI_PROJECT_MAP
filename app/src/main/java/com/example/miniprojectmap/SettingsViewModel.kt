package com.example.miniprojectmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Pakai AndroidViewModel karena butuh "Context/Application" untuk akses DataStore
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application)

    // Mengubah Flow menjadi State yang bisa dibaca UI
    val isDarkMode: StateFlow<Boolean> = dataStore.isDarkModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Fungsi untuk mengubah tema (dipanggil saat Switch diklik)
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.saveThemeSetting(isDark)
        }
    }
}