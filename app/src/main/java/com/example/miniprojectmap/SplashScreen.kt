package com.example.miniprojectmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToNext: (Boolean) -> Unit) {
    LaunchedEffect(key1 = true) {
        delay(3000) // Tahan 3 detik
        val auth = FirebaseAuth.getInstance()
        onNavigateToNext(auth.currentUser != null)
    }

    Box(
        // FIX: Background mengikuti tema (Hitam pekat saat Dark Mode)
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                // Tips: Bisa ganti Icons.Default.AccountCircle dengan Icons.Default.Cake jika suka
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                // FIX: Warna Ikon mengikuti Primary Theme (Bukan hijau statis)
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HUT Komunitas Tracker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                // FIX: Warna Teks kontras dengan background (Putih di Dark, Hitam di Light)
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}