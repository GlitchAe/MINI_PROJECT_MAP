package com.example.miniprojectmap

import com.google.firebase.firestore.FirebaseFirestore

class BirthdayRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. Ambil Semua User yang Register di Aplikasi (Otomatis)
    fun getAllRegisteredUsers(onResult: (List<Person>) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { doc ->
                    // User register pakai field "fullName"
                    val name = doc.getString("fullName")
                    val date = doc.getString("birthDate")

                    if (!name.isNullOrEmpty() && !date.isNullOrEmpty()) {
                        Person(
                            id = doc.id,
                            name = name,
                            birthDate = date,
                            isManual = false // Ini user asli, tidak bisa dihapus dari list
                        )
                    } else null
                }
                onResult(users)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // 2. Ambil Teman yang Kita Input Sendiri (Manual)
    fun getMyFriends(userId: String, onResult: (List<Person>) -> Unit) {
        db.collection("users").document(userId).collection("friends")
            .get()
            .addOnSuccessListener { snapshot ->
                val friends = snapshot.documents.mapNotNull { doc ->
                    // Teman manual pakai field "name" (sesuai input dialog)
                    val name = doc.getString("name")
                    val date = doc.getString("birthDate")

                    if (!name.isNullOrEmpty() && !date.isNullOrEmpty()) {
                        Person(
                            id = doc.id,
                            name = name,
                            birthDate = date,
                            isManual = true // Ini manual, bisa dihapus
                        )
                    } else null
                }
                onResult(friends)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // 3. Tambah Teman Manual
    fun addBirthday(userId: String, person: Person, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf(
            "name" to person.name,
            "birthDate" to person.birthDate,
            "isManual" to true
        )

        db.collection("users").document(userId).collection("friends")
            .add(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // 4. Hapus Teman Manual
    fun deleteBirthday(userId: String, personId: String, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(userId).collection("friends").document(personId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}