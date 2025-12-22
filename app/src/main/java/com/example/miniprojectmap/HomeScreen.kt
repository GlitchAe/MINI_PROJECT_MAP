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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToBirthday: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // FIX: Gunakan Warna dari Tema Aplikasi (Bukan System HP)
    val mainBg = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = mainBg,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = contentColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // KARTU WELCOME
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp).shadow(16.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(GradientRoyal)
            ) {
                Box(Modifier.offset(x = (-20).dp, y = (-20).dp).size(100.dp).clip(CircleShape).background(Color.White.copy(0.1f)))
                Box(Modifier.align(Alignment.BottomEnd).offset(x = 20.dp, y = 30.dp).size(120.dp).clip(CircleShape).background(Color.White.copy(0.1f)))
                Row(modifier = Modifier.padding(24.dp).align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(50.dp), tint = Color.White.copy(0.9f))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Halo, ${uiState.userName}!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(uiState.quote, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.White.copy(0.8f), lineHeight = 16.sp, maxLines = 2)
                        Text(uiState.author, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.6f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Highlight Hari Ini", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = contentColor)
            Spacer(modifier = Modifier.height(16.dp))

            // KARTU ULTAH
            Box(
                modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(GradientSunset)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color.Yellow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ulang Tahun Berikutnya", fontSize = 14.sp, color = Color.White.copy(0.9f), fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val nearest = uiState.nearestPerson
                    if (nearest != null) {
                        Text(nearest.name, fontWeight = FontWeight.Black, fontSize = 32.sp, color = Color.White)
                        val nextDate = DateUtils.getNextBirthday(nearest.birthDate)
                        val dateStr = if (nextDate != null) SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(nextDate) else nearest.birthDate
                        Text(dateStr, color = Color.White.copy(0.9f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(50)) {
                            Text(text = if (uiState.daysRemaining <= 0L) "🎉 HARI INI!" else "⏳ ${uiState.daysRemaining} Hari Lagi", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    } else {
                        Text("Tidak ada data", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ColorfulMenuCard("Kalender", Icons.Default.DateRange, GradientOcean, onNavigateToCalendar, Modifier.weight(1f))
                ColorfulMenuCard("Daftar Ultah", Icons.AutoMirrored.Filled.List, GradientMint, onNavigateToBirthday, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ColorfulMenuCard(title: String, icon: ImageVector, brush: Brush, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(110.dp).shadow(8.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(brush).clickable { onClick() }) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(40.dp).background(Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        }
    }
}