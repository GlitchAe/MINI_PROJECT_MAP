package com.example.miniprojectmap

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "Loading...",
    val nearestPerson: Person? = null,
    val ageNext: Int = 0,
    val daysRemaining: Long = 0,
    // TAMBAHAN: Data Quote
    val quote: String = "Mengambil motivasi...",
    val author: String = ""
)

class HomeViewModel : ViewModel() {
    private val repo = BirthdayRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        fetchDailyQuote() // <--- Panggil fungsi baru ini
    }

    // FUNGSI BARU: HIT API QUOTE
    private fun fetchDailyQuote() {
        viewModelScope.launch {
            try {
                // Panggil Retrofit
                val response = ApiConfig.getApiService().getRandomQuote()

                // Update UI State
                _uiState.value = _uiState.value.copy(
                    quote = "\"${response.quote}\"",
                    author = "- ${response.author}"
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Gagal ambil quote: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    quote = "Gagal memuat motivasi.",
                    author = "Cek internetmu ya"
                )
            }
        }
    }

    private fun loadDashboardData() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                var myProfile: Person? = null
                var userName = "User"

                if (document != null && document.exists()) {
                    userName = document.getString("fullName") ?: "User"
                    val myDate = document.getString("birthDate") ?: ""
                    if (myDate.isNotEmpty()) {
                        myProfile = Person(id = uid, name = "$userName (Saya)", birthDate = myDate, userId = uid)
                    }
                }
                _uiState.value = _uiState.value.copy(userName = userName)

                repo.getBirthdays(uid) { friends ->
                    calculateNearestBirthday(friends, myProfile)
                }
            }
        }
    }

    private fun calculateNearestBirthday(friends: List<Person>, myProfile: Person?) {
        val allPeople = friends + listOfNotNull(myProfile)

        if (allPeople.isNotEmpty()) {
            val sorted = allPeople.sortedBy { person ->
                DateUtils.getNextBirthday(person.birthDate)?.time ?: Long.MAX_VALUE
            }

            val nearest = sorted.firstOrNull()
            if (nearest != null) {
                val ageNext = DateUtils.getAgeOnNextBirthday(nearest.birthDate)
                val nextDate = DateUtils.getNextBirthday(nearest.birthDate)
                var daysRem = 0L

                if (nextDate != null) {
                    daysRem = (nextDate.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
                }

                _uiState.value = _uiState.value.copy(
                    nearestPerson = nearest,
                    ageNext = ageNext,
                    daysRemaining = daysRem
                )
            }
        }
    }
}