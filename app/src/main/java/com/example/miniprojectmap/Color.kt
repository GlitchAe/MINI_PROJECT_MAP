package com.example.miniprojectmap

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Palet Warna Modern
val PrimaryBlue = Color(0xFF0D47A1)
val LightBlue = Color(0xFF42A5F5)
val TealAccent = Color(0xFF26A69A)
val MintGreen = Color(0xFF80CBC4)
val OffWhite = Color(0xFFF5F7FA) // Background biar ga putih polos sakit mata
val CoralRed = Color(0xFFFF7043) // Untuk tombol Hapus/Logout

// Gradasi Keren
val BlueGradient = Brush.horizontalGradient(listOf(PrimaryBlue, LightBlue))
val TealGradient = Brush.horizontalGradient(listOf(Color(0xFF00695C), TealAccent))
val SunsetGradient = Brush.horizontalGradient(listOf(Color(0xFFEF5350), Color(0xFFFFCA28)))