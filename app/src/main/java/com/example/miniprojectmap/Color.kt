package com.example.miniprojectmap

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- PALET WARNA MODERN ---

// 1. Primary (Ungu - Biru Elektrik)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// 2. Gradasi Keren (Untuk Kartu & Button)
// a. Ocean Blue (Untuk Header/Home)
val GradientOcean = Brush.linearGradient(
    listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
)

// b. Sunset Orange (Untuk Banana/Peringatan)
val GradientSunset = Brush.linearGradient(
    listOf(Color(0xFFFF512F), Color(0xFFDD2476))
)

// c. Fresh Mint (Untuk Tombol Sukses/Riwayat)
val GradientMint = Brush.linearGradient(
    listOf(Color(0xFF11998e), Color(0xFF38ef7d))
)

// d. Royal Purple (Untuk Highlight/Ultah)
val GradientRoyal = Brush.linearGradient(
    listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
)

// 3. Background (Supaya tidak Putih/Hitam polos)
val BgLight = Color(0xFFF0F4F8) // Putih kebiruan (Clean)
val BgDark = Color(0xFF121212)  // Hitam pekat tapi soft
val SurfaceDark = Color(0xFF1E1E1E) // Abu gelap untuk kartu di dark mode