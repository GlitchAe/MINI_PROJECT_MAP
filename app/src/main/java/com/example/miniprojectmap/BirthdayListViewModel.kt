package com.example.miniprojectmap

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BirthdayListViewModel : ViewModel() {
    private val repo = BirthdayRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // State list yang sudah digabung dan disortir
    private val _birthdayList = MutableStateFlow<List<Person>>(emptyList())
    val birthdayList: StateFlow<List<Person>> = _birthdayList.asStateFlow()

    init {
        loadBirthdays()
    }

    private fun loadBirthdays() {
        val uid = auth.currentUser?.uid ?: return

        // Ambil Data Teman (Realtime dari Repo)
        repo.getBirthdays(uid) { friends ->
            // Ambil Data Diri Sendiri (One-shot)
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                var myProfile: Person? = null
                if (doc.exists()) {
                    val name = doc.getString("fullName") ?: "Saya"
                    val date = doc.getString("birthDate") ?: ""
                    if (date.isNotEmpty()) {
                        myProfile = Person(id = uid, name = "$name (Saya)", birthDate = date, userId = uid)
                    }
                }

                // Gabung & Sortir
                processList(friends, myProfile)
            }
        }
    }

    private fun processList(friends: List<Person>, myProfile: Person?) {
        val rawList = friends + listOfNotNull(myProfile)
        val sorted = rawList.sortedBy { person ->
            DateUtils.getNextBirthday(person.birthDate)?.time ?: Long.MAX_VALUE
        }
        _birthdayList.value = sorted
    }

    fun addFriend(name: String, date: String) {
        val uid = auth.currentUser?.uid ?: return
        repo.addBirthday(uid, name, date,
            onSuccess = { /* Data akan otomatis refresh karena listener di repo */ },
            onFailure = { /* Handle error jika perlu */ }
        )
    }

    fun deleteFriend(id: String) {
        repo.deleteBirthday(id)
    }
}