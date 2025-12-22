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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen() {
    val repo = remember { BirthdayRepository() }
    val auth = FirebaseAuth.getInstance()

    // Tema Aplikasi
    val bgColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Data
    var allPeople by remember { mutableStateOf<List<Person>>(emptyList()) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var birthdaysOnSelectedDate by remember { mutableStateOf<List<Person>>(emptyList()) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            repo.getAllRegisteredUsers { globalUsers ->
                repo.getMyFriends(uid) { myFriends ->
                    allPeople = (globalUsers + myFriends).distinctBy { it.name + it.birthDate }
                }
            }
        }
    }

    LaunchedEffect(allPeople, selectedDate) {
        birthdaysOnSelectedDate = allPeople.filter { person ->
            DateUtils.isBirthdayOnDate(
                person.birthDate,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(bgColor).padding(16.dp)) {

        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { val n = currentMonth.clone() as Calendar; n.add(Calendar.MONTH, -1); currentMonth = n },
                modifier = Modifier.background(Color.Gray.copy(0.1f), CircleShape)
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev", tint = contentColor) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(SimpleDateFormat("MMMM", Locale.getDefault()).format(currentMonth.time), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = contentColor)
                Text(SimpleDateFormat("yyyy", Locale.getDefault()).format(currentMonth.time), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = contentColor.copy(0.6f))
            }

            IconButton(
                onClick = { val n = currentMonth.clone() as Calendar; n.add(Calendar.MONTH, 1); currentMonth = n },
                modifier = Modifier.background(Color.Gray.copy(0.1f), CircleShape)
            ) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next", tint = contentColor) }
        }

        // --- HARI ---
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (day == "Min") Color(0xFFFF5252) else contentColor.copy(0.5f))
            }
        }

        // --- GRID TANGGAL ---
        val daysInMonth = getDaysInMonth(currentMonth)

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(340.dp)) {
            // FIX: Menggunakan items(items = ...)
            items(items = daysInMonth) { dayInfo ->
                if (dayInfo == null) {
                    Box(modifier = Modifier.size(45.dp))
                } else {
                    val date: Calendar = dayInfo
                    val isSelected = isSameDay(selectedDate, date)
                    val isToday = isToday(date)
                    val hasEvent = allPeople.any { DateUtils.isBirthdayOnDate(it.birthDate, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)) }

                    val backgroundBrush: Brush = if (isSelected) GradientRoyal else if (isToday) SolidColor(contentColor.copy(0.1f)) else SolidColor(Color.Transparent)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(backgroundBrush)
                            .clickable { selectedDate = date }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.get(Calendar.DAY_OF_MONTH).toString(),
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else contentColor
                            )
                            if (hasEvent) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isSelected) SolidColor(Color.White) else GradientSunset))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- LIST ACARA ---
        Text("Acara ${SimpleDateFormat("dd MMMM", Locale.getDefault()).format(selectedDate.time)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = contentColor)
        Spacer(modifier = Modifier.height(12.dp))

        if (birthdaysOnSelectedDate.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                Text("Tidak ada acara.", color = contentColor.copy(0.5f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // FIX: Menggunakan items(items = ...)
                items(items = birthdaysOnSelectedDate) { person ->
                    val age = DateUtils.getAgeInYear(person.birthDate, selectedDate.get(Calendar.YEAR))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceColor)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(GradientOcean), contentAlignment = Alignment.Center) {
                                val initial = if (person.name.isNotEmpty()) person.name.first().toString().uppercase() else "?"
                                Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(person.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = contentColor)
                                Text("Ulang tahun ke-$age", fontSize = 12.sp, color = contentColor.copy(0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER FUNCTIONS (WAJIB ADA) ---
fun getDaysInMonth(currentMonth: Calendar): List<Calendar?> {
    val days = mutableListOf<Calendar?>()
    val cal = currentMonth.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    for (i in 1 until firstDayOfWeek) { days.add(null) }
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
    return isSameDay(cal, Calendar.getInstance())
}