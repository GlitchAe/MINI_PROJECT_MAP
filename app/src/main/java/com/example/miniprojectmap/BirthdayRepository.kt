package com.example.miniprojectmap

import com.google.firebase.firestore.FirebaseFirestore

class BirthdayRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("birthdays")

    // Tambah Data
    fun addBirthday(
        userId: String,
        name: String,
        date: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val newId = collectionRef.document().id
        val person = Person(id = newId, name = name, birthDate = date, userId = userId)

        collectionRef.document(newId)
            .set(person)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Ambil Data (Realtime)
    fun getBirthdays(userId: String, onDataChanged: (List<Person>) -> Unit) {
        collectionRef
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val list = snapshot.toObjects(Person::class.java)
                onDataChanged(list)
            }
    }

    // Hapus Data
    fun deleteBirthday(documentId: String) {
        collectionRef.document(documentId).delete()
    }
}