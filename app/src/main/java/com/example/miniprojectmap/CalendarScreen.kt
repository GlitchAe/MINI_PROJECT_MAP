package com.example.miniprojectmap

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun CalendarScreen() {
    val repo = remember { BirthdayRepository() }
    val auth = FirebaseAuth.getInstance()

    // Data semua teman dari database
    var allBirthdays by remember { mutableStateOf<List<Person>>(emptyList()) }

    // Data ulang tahun yang COCOK dengan tanggal yang dipilih
    var birthdaysOnSelectedDate by remember { mutableStateOf<List<Person>>(emptyList()) }

    // State tanggal yang sedang dipilih (Default: Hari ini)
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    // 1. Ambil data dari Firestore saat layar dibuka
    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            repo.getBirthdays(uid) { list ->
                allBirthdays = list
                // Cek apakah hari ini ada yang ulang tahun?
                birthdaysOnSelectedDate = list.filter {
                    DateUtils.isBirthdayOnDate(it.birthDate, selectedYear, selectedMonth, selectedDay)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- BAGIAN KALENDER ---
        AndroidView(
            factory = { context ->
                CalendarView(context).apply {
                    // Update tampilan saat tanggal diklik
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        selectedYear = year
                        selectedMonth = month
                        selectedDay = dayOfMonth

                        // Filter: Cari siapa yang ulang tahun di tanggal ini
                        birthdaysOnSelectedDate = allBirthdays.filter { person ->
                            DateUtils.isBirthdayOnDate(person.birthDate, year, month, dayOfMonth)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        )

        // --- BAGIAN DAFTAR ACARA DI BAWAHNYA ---
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Acara Tanggal $selectedDay/${selectedMonth + 1}/$selectedYear",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (birthdaysOnSelectedDate.isEmpty()) {
                Text("Tidak ada ulang tahun di tanggal ini.", color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(birthdaysOnSelectedDate) { person ->
                        val age = DateUtils.getAgeInYear(person.birthDate, selectedYear)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // Warna kuning ultah
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🎂 Ulang Tahun ${person.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Menjadi umur $age tahun",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}