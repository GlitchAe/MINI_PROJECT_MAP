package com.example.miniprojectmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen() {
    val repo = remember { BirthdayRepository() }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Data
    var friendsList by remember { mutableStateOf<List<Person>>(emptyList()) }
    var myProfile by remember { mutableStateOf<Person?>(null) }
    var allPeople by remember { mutableStateOf<List<Person>>(emptyList()) }

    // State Kalender Custom
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    // List ultah di tanggal yang dipilih
    var birthdaysOnSelectedDate by remember { mutableStateOf<List<Person>>(emptyList()) }

    // 1. Ambil Data
    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            repo.getBirthdays(uid) { list -> friendsList = list }
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("fullName") ?: "Saya"
                    val date = doc.getString("birthDate") ?: ""
                    if (date.isNotEmpty()) {
                        myProfile = Person(id = uid, name = "$name (Saya)", birthDate = date, userId = uid)
                    }
                }
            }
        }
    }

    // 2. Gabung Data & Update Filter saat tanggal dipilih
    LaunchedEffect(friendsList, myProfile, selectedDate) {
        allPeople = friendsList + listOfNotNull(myProfile)

        // Filter orang yang ultah di tanggal yang diklik
        birthdaysOnSelectedDate = allPeople.filter { person ->
            DateUtils.isBirthdayOnDate(
                person.birthDate,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)).padding(16.dp)) {

        // --- HEADER KALENDER (Bulan & Tahun) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newCal = currentMonth.clone() as Calendar
                newCal.add(Calendar.MONTH, -1)
                currentMonth = newCal
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev")
            }

            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(currentMonth.time),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1)
            )

            IconButton(onClick = {
                val newCal = currentMonth.clone() as Calendar
                newCal.add(Calendar.MONTH, 1)
                currentMonth = newCal
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next")
            }
        }

        // --- NAMA HARI (Minggu - Sabtu) ---
        val daysOfWeek = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- GRID TANGGAL ---
        // Hitung logika kalender (jumlah hari, padding awal bulan)
        val daysInMonth = getDaysInMonth(currentMonth)

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(320.dp) // Batasi tinggi grid
        ) {
            items(daysInMonth) { dayInfo ->
                if (dayInfo == null) {
                    // Kotak Kosong (Padding)
                    Box(modifier = Modifier.size(40.dp))
                } else {
                    // Kotak Tanggal
                    val isSelected = isSameDay(selectedDate, dayInfo)
                    val isToday = isToday(dayInfo)

                    // Cek apakah ada yang ultah di tanggal ini? (Untuk TITIK MERAH)
                    val hasEvent = allPeople.any {
                        DateUtils.isBirthdayOnDate(it.birthDate, dayInfo.get(Calendar.YEAR), dayInfo.get(Calendar.MONTH), dayInfo.get(Calendar.DAY_OF_MONTH))
                    }

                    DayCell(
                        date = dayInfo,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasEvent = hasEvent,
                        onClick = { selectedDate = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DAFTAR DETAIL ACARA ---
        Text(
            text = "Acara Tanggal ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(selectedDate.time)}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF0D47A1)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (birthdaysOnSelectedDate.isEmpty()) {
            Text("Tidak ada acara.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        } else {
            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(birthdaysOnSelectedDate) { person ->
                    val age = DateUtils.getAgeInYear(person.birthDate, selectedDate.get(Calendar.YEAR))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🎂", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(person.name, fontWeight = FontWeight.Bold)
                                Text("Ulang tahun ke-$age", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- KOMPONEN KOTAK TANGGAL ---
@Composable
fun DayCell(
    date: Calendar,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onClick: (Calendar) -> Unit
) {
    val dayNumber = date.get(Calendar.DAY_OF_MONTH).toString()

    // Warna Background
    val bgColor = when {
        isSelected -> Color(0xFF0D47A1) // Biru Tua kalau dipilih
        isToday -> Color(0xFFE3F2FD)    // Biru Muda kalau hari ini
        else -> Color.Transparent
    }

    // Warna Teks
    val textColor = if (isSelected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f) // Biar kotak
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = dayNumber, color = textColor, fontWeight = FontWeight.Medium)

            // TITIK MERAH (EVENT)
            if (hasEvent) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0xFFFF5252)) // Merah (atau putih jika selected)
                )
            }
        }
    }
}

// --- LOGIKA HITUNG HARI (HELPER) ---
fun getDaysInMonth(currentMonth: Calendar): List<Calendar?> {
    val days = mutableListOf<Calendar?>()

    // Kloning biar gak ngerusak state asli
    val cal = currentMonth.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1) // Set ke tanggal 1

    // Cari tahu tanggal 1 itu hari apa (Minggu=1, Senin=2, ...)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1..7

    // Tambah padding kosong sebelum tanggal 1
    // (Misal tanggal 1 hari Rabu (4), berarti butuh 3 kotak kosong)
    for (i in 1 until firstDayOfWeek) {
        days.add(null)
    }

    // Tambah hari-hari dalam bulan
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..maxDays) {
        val dayCal = cal.clone() as Calendar
        dayCal.set(Calendar.DAY_OF_MONTH, i)
        days.add(dayCal)
    }

    return days
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

fun isToday(cal: Calendar): Boolean {
    val today = Calendar.getInstance()
    return isSameDay(cal, today)
}