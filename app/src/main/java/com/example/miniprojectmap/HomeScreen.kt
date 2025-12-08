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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
    // HAPUS PARAMETER SCANNER & HISTORY (Karena sudah pindah ke menu sendiri)
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // KARTU 1: WELCOME
            Card(
                modifier = Modifier.fillMaxWidth().height(140.dp), // Perbesar sedikit tingginya
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(BlueGradient)) {
                    Row(
                        modifier = Modifier.padding(20.dp).align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(40.dp), tint = Color.White)
                        }

                        // --- BAGIAN INI DIUPDATE ---
                        Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                            Text("Halo, ${uiState.userName}!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                            Spacer(modifier = Modifier.height(4.dp))

                            // Tampilkan Quote dari API
                            Text(
                                text = uiState.quote,
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp,
                                maxLines = 3
                            )
                            Text(
                                text = uiState.author,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // KARTU 2: NEAREST BIRTHDAY
            Text("Acara Terdekat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(TealGradient)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ulang Tahun Berikutnya", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val nearest = uiState.nearestPerson

                        if (nearest != null) {
                            Text(nearest.name, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Color.White)

                            val nextDate = DateUtils.getNextBirthday(nearest.birthDate)
                            val fmt = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                            val dateStr = if (nextDate != null) fmt.format(nextDate) else nearest.birthDate

                            Text(dateStr, color = Color.White, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = if (uiState.daysRemaining <= 0L) "🎉 HARI INI KE-${uiState.ageNext}! 🎂" else "⏳ H-${uiState.daysRemaining} menuju ke-${uiState.ageNext}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Text("Belum ada data", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MENU GRID (Memanggil DashboardMenuCard)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardMenuCard("Kalender", Icons.Default.DateRange, Color(0xFF5C6BC0), onNavigateToCalendar, Modifier.weight(1f))
                DashboardMenuCard("Daftar Ultah", Icons.AutoMirrored.Filled.List, Color(0xFFAB47BC), onNavigateToBirthday, Modifier.weight(1f))
            }

            // BAGIAN BANADOC (KARTU KUNING) SUDAHDIHAPUS DARI SINI
            // KARENA SUDAH PINDAH KE HALAMAN 'BanaDocMenuScreen'
        }
    }
}

// Helper Function (Tetap Ada)
@Composable
fun DashboardMenuCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
        }
    }
}