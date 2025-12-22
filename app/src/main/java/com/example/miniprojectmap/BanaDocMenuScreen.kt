package com.example.miniprojectmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// PERHATIKAN NAMA PARAMETER DI SINI:
@Composable
fun BanaDocMenuScreen(
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // JUDUL
        Text(
            text = "BanaDoc AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor
        )
        Text(
            text = "Deteksi Penyakit Pisang",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // MENU 1: SCAN (Besar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shadow(10.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784))))
                .clickable { onScanClick() }, // PANGGIL CALLBACK INI
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Mulai Diagnosa", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Ambil foto daun pisang", fontSize = 14.sp, color = Color.White.copy(0.8f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MENU 2: HISTORY (Kecil)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onHistoryClick() }, // PANGGIL CALLBACK INI
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.History, null, tint = Color(0xFFFF9800))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Riwayat Scan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
                    Text("Lihat hasil sebelumnya", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}