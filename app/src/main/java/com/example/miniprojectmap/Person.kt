package com.example.miniprojectmap

data class Person(
    var id: String = "",       // ID dokumen dari Firestore
    val name: String = "",     // Nama teman
    val birthDate: String = "", // Tanggal lahir
    val userId: String = ""    // ID user pemilik data ini
)