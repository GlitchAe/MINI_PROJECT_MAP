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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToBirthday: () -> Unit,
    // --- 1. PARAMETER BARU UNTUK BANADOC ---
    onNavigateToScanner: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val repo = remember { BirthdayRepository() }

    var userName by remember { mutableStateOf("Loading...") }
    var nearestPerson by remember { mutableStateOf<Person?>(null) }
    var ageNext by remember { mutableStateOf(0) }
    var daysRemaining by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                var myProfile: Person? = null
                if (document != null && document.exists()) {
                    userName = document.getString("fullName") ?: "User"
                    val myDate = document.getString("birthDate") ?: ""
                    if (myDate.isNotEmpty()) {
                        myProfile = Person(id = uid, name = "$userName (Saya)", birthDate = myDate, userId = uid)
                    }
                }
                repo.getBirthdays(uid) { friends ->
                    val allPeople = friends + listOfNotNull(myProfile)
                    if (allPeople.isNotEmpty()) {
                        val sorted = allPeople.sortedBy { person ->
                            DateUtils.getNextBirthday(person.birthDate)?.time ?: Long.MAX_VALUE
                        }
                        val nearest = sorted.firstOrNull()
                        if (nearest != null) {
                            nearestPerson = nearest
                            ageNext = DateUtils.getAgeOnNextBirthday(nearest.birthDate)
                            val nextDate = DateUtils.getNextBirthday(nearest.birthDate)
                            if (nextDate != null) {
                                daysRemaining = (nextDate.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
                            }
                        }
                    }
                }
            }
        }
    }

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
                modifier = Modifier.fillMaxWidth().height(120.dp),
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
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text("Halo, $userName!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Semoga harimu menyenangkan ✨", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
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

                        if (nearestPerson != null) {
                            Text(nearestPerson!!.name, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Color.White)

                            val nextDate = DateUtils.getNextBirthday(nearestPerson!!.birthDate)
                            val fmt = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                            val dateStr = if (nextDate != null) fmt.format(nextDate) else nearestPerson!!.birthDate

                            Text(dateStr, color = Color.White, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Badge Umur
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = if (daysRemaining <= 0L) "🎉 HARI INI KE-$ageNext! 🎂" else "⏳ H-$daysRemaining menuju ke-$ageNext",
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

            // MENU GRID (KALENDER & LIST)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardMenuCard(
                    title = "Kalender",
                    icon = Icons.Default.DateRange,
                    color = Color(0xFF5C6BC0),
                    onClick = onNavigateToCalendar,
                    modifier = Modifier.weight(1f)
                )
                DashboardMenuCard(
                    title = "Daftar Ultah",
                    icon = Icons.AutoMirrored.Filled.List,
                    color = Color(0xFFAB47BC),
                    onClick = onNavigateToBirthday,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. FITUR BARU: BANADOC (DI PALING BAWAH) ---
            Text("BanaDoc AI", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text("Deteksi penyakit pisang otomatis", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF176)) // Kuning Pisang
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Tombol Scanner
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToScanner() }) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scanner", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                    }

                    // Tombol Riwayat
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToHistory() }) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Riwayat", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                    }
                }
            }

            // Padding bawah agar tidak tertutup Navbar
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// Komponen Kecil untuk Tombol Menu Kotak
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