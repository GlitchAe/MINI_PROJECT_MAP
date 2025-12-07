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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToNext: (Boolean) -> Unit) {
    LaunchedEffect(key1 = true) {
        delay(3000)
        val auth = FirebaseAuth.getInstance()
        onNavigateToNext(auth.currentUser != null)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFE3F2FD)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color(0xFF00695C)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HUT Komunitas Tracker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
        }
    }
}