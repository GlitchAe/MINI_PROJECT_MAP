package com.example.miniprojectmap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanaDocMenuScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    // Gradasi Khusus BanaDoc (Kuning ke Oranye)
    val bananaGradient = Brush.verticalGradient(
        listOf(Color(0xFFFFF176), Color(0xFFFFB74D))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BanaDoc AI", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFF9C4))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Info
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Gunakan fitur ini untuk mendeteksi penyakit pada tanaman pisang menggunakan AI.",
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1)
                    )
                }
            }

            // MENU 1: SCANNER
            BigMenuButton(
                title = "Mulai Scan",
                desc = "Ambil foto daun/buah pisang",
                icon = Icons.Default.CameraAlt,
                color = Color(0xFFFFB74D),
                onClick = onNavigateToScanner
            )

            Spacer(modifier = Modifier.height(24.dp))

            // MENU 2: RIWAYAT
            BigMenuButton(
                title = "Riwayat Diagnosa",
                desc = "Lihat hasil scan sebelumnya",
                icon = Icons.Default.History,
                color = Color(0xFF81C784),
                onClick = onNavigateToHistory
            )
        }
    }
}

@Composable
fun BigMenuButton(title: String, desc: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(desc, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            }
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
    }
}