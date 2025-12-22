package com.example.miniprojectmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State untuk UI Home
data class HomeUiState(
    val userName: String = "Loading...",
    val quote: String = "Mengambil motivasi...",
    val author: String = "",
    val nearestPerson: Person? = null,
    val ageNext: Int = 0,
    val daysRemaining: Long = 0
)

class HomeViewModel : ViewModel() {
    private val repo = BirthdayRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        fetchDailyQuote()
    }

    private fun fetchDailyQuote() {
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getRandomQuote()
                _uiState.value = _uiState.value.copy(
                    quote = "\"${response.quote}\"",
                    author = "- ${response.author}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(quote = "Gagal memuat motivasi.", author = "")
            }
        }
    }

    private fun loadDashboardData() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Ambil Nama User yang Login (untuk sapaan "Halo, X")
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            val name = document?.getString("fullName") ?: "User"
            _uiState.value = _uiState.value.copy(userName = name)
        }

        // 2. GABUNGKAN DATA: (Semua User Global) + (Teman yang saya input)
        repo.getAllRegisteredUsers { globalUsers ->
            repo.getMyFriends(uid) { myFriends ->

                // Gabung dan hapus duplikat (jika ada ID yang sama)
                // Kita filter 'distinctBy' nama & tgl lahir untuk jaga-jaga
                val allPeople = (globalUsers + myFriends).distinctBy { it.name + it.birthDate }

                calculateNearestBirthday(allPeople)
            }
        }
    }

    private fun calculateNearestBirthday(people: List<Person>) {
        if (people.isNotEmpty()) {
            val sorted = people.sortedBy { person ->
                DateUtils.getNextBirthday(person.birthDate)?.time ?: Long.MAX_VALUE
            }
            val nearest = sorted.firstOrNull()
            if (nearest != null) {
                val nextDate = DateUtils.getNextBirthday(nearest.birthDate)
                val ageNext = DateUtils.getAgeOnNextBirthday(nearest.birthDate)
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