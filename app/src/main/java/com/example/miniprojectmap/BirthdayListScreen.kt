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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayListScreen() {
    val repo = remember { BirthdayRepository() }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // State Data Asli
    var friendsList by remember { mutableStateOf<List<Person>>(emptyList()) }
    var myProfile by remember { mutableStateOf<Person?>(null) }

    // State Hasil Gabungan & Filter
    var displayList by remember { mutableStateOf<List<Person>>(emptyList()) }

    // State UI
    var searchQuery by remember { mutableStateOf("") } // <-- STATE PENCARIAN
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // 1. AMBIL DATA
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

    // 2. LOGIKA SORTING & SEARCHING (PENTING!)
    LaunchedEffect(friendsList, myProfile, searchQuery) {
        val rawList = friendsList + listOfNotNull(myProfile)

        // A. Sorting (Terdekat)
        val sortedList = rawList.sortedBy { person ->
            val date = DateUtils.parseDate(person.birthDate)
            if (date != null) {
                val cal = Calendar.getInstance()
                cal.time = date
                cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
                if (cal.timeInMillis < System.currentTimeMillis() - 86400000) cal.add(Calendar.YEAR, 1)
                cal.timeInMillis
            } else { Long.MAX_VALUE }
        }

        // B. Searching (Filter Nama)
        if (searchQuery.isEmpty()) {
            displayList = sortedList
        } else {
            displayList = sortedList.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // LOGIKA DATE PICKER
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        newDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFF26A69A), contentColor = Color.White) {
                Icon(Icons.Default.Add, "Tambah")
            }
        },
        containerColor = Color(0xFFF5F7FA) // OffWhite
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {

            // --- HEADER & SEARCH BAR ---
            Text("Jadwal Ulang Tahun", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
            Spacer(modifier = Modifier.height(16.dp))

            // Kolom Pencarian
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari nama teman...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF26A69A),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- LIST ---
            if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isNotEmpty()) "Nama tidak ditemukan" else "Belum ada data",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(displayList) { person ->
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val age = DateUtils.getAgeInYear(person.birthDate, currentYear)
                        val zodiac = DateUtils.getZodiac(person.birthDate) // <-- AMBIL ZODIAK
                        val isMe = person.id == auth.currentUser?.uid

                        BirthdayItemZodiac(
                            person = person,
                            age = age,
                            zodiac = zodiac, // <-- KIRIM KE UI
                            isMe = isMe,
                            onDelete = { if (!isMe) repo.deleteBirthday(person.id) }
                        )
                    }
                }
            }
        }

        // DIALOG TAMBAH (SAMA)
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Tambah Teman") },
                text = {
                    Column {
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newDate, onValueChange = {}, label = { Text("Tanggal Lahir") },
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            readOnly = true, enabled = false,
                            trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Pilih") } },
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Black, disabledTrailingIconColor = Color.Black)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newName.isNotEmpty() && newDate.isNotEmpty() && auth.currentUser != null) {
                            repo.addBirthday(auth.currentUser!!.uid, newName, newDate, {}, {})
                            showDialog = false
                            newName = ""; newDate = ""
                        }
                    }) { Text("Simpan") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
            )
        }
    }
}

@Composable
fun BirthdayItemZodiac(person: Person, age: Int, zodiac: String, isMe: Boolean, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon Lingkaran (Inisial)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isMe) Color(0xFF42A5F5) else Color(0xFF80CBC4)),
                contentAlignment = Alignment.Center
            ) {
                if (isMe) Icon(Icons.Default.Star, null, tint = Color.White)
                else Text(person.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(person.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)

                // Baris Info: Tanggal + Zodiak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(person.birthDate, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))

                    // Chip Zodiak
                    if (zodiac.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFFF9C4), // Background tetap Kuning Muda biar vibes-nya cerah
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(20.dp),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                // PERUBAHAN DI SINI: Color.Black
                                Text(zodiac, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // Badge Umur
            Surface(
                color = Color(0xFFF5F7FA),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("$age Thn", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
            }

            if (!isMe) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Hapus", tint = Color(0xFFFF7043))
                }
            }
        }
    }
}