package com.example.miniprojectmap

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BirthdayListViewModel : ViewModel() {
    private val repo = BirthdayRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<List<Person>>(emptyList())
    val uiState: StateFlow<List<Person>> = _uiState.asStateFlow()

    init {
        loadBirthdays()
    }

    private fun loadBirthdays() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Ambil User Register (Otomatis)
        repo.getAllRegisteredUsers { globalUsers ->
            // 2. Ambil Teman Manual
            repo.getMyFriends(uid) { myFriends ->
                // Gabungkan (Prioritas User Register jika ada duplikat nama)
                val combinedList = (globalUsers + myFriends)
                    .distinctBy { it.name + it.birthDate }
                    .sortedBy { it.name }

                _uiState.value = combinedList
            }
        }
    }

    fun addBirthday(name: String, date: String) {
        val uid = auth.currentUser?.uid ?: return
        // Teman manual -> isManual = true
        val newPerson = Person(name = name, birthDate = date, isManual = true)

        repo.addBirthday(uid, newPerson) { success ->
            if (success) loadBirthdays()
        }
    }

    fun deleteBirthday(personId: String) {
        val uid = auth.currentUser?.uid ?: return
        repo.deleteBirthday(uid, personId) { success ->
            if (success) loadBirthdays()
        }
    }
}