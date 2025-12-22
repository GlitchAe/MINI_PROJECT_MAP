package com.example.miniprojectmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanaDocMenuScreen(
    onNavigateToGallery: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    // FIX: Gunakan Tema Aplikasi
    val bgColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("BanaDoc AI", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = contentColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(surfaceColor)) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔍", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Deteksi penyakit pisang menggunakan AI. Pilih foto daun atau buah untuk mulai.", fontSize = 14.sp, color = contentColor.copy(0.7f), lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ActionCard("Mulai Scan", "Ambil Foto / Galeri", Icons.Default.Image, GradientSunset, onNavigateToGallery)
            Spacer(modifier = Modifier.height(20.dp))
            ActionCard("Riwayat Diagnosa", "Lihat hasil scan lama", Icons.Default.History, GradientOcean, onNavigateToHistory)
        }
    }
}

@Composable
fun ActionCard(title: String, desc: String, icon: ImageVector, brush: Brush, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(140.dp).shadow(10.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(brush).clickable { onClick() }) {
        Box(Modifier.align(Alignment.TopEnd).offset(x = 20.dp, y = (-20).dp).size(80.dp).background(Color.White.copy(0.1f), CircleShape))
        Row(modifier = Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, fontSize = 14.sp, color = Color.White.copy(0.8f))
            }
            Box(modifier = Modifier.size(56.dp).background(Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}