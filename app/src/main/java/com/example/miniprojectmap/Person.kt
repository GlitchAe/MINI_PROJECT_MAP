package com.example.miniprojectmap

data class Person(
    val id: String = "",
    val name: String = "",
    val birthDate: String = "",
    // Field baru: Default false (berarti dari User Register/Otomatis)
    val isManual: Boolean = false
)