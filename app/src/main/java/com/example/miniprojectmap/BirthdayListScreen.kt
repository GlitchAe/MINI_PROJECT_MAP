package com.example.miniprojectmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun BirthdayListScreen() {
    val repo = remember { BirthdayRepository() }
    val auth = FirebaseAuth.getInstance()

    // State Data
    var birthdayList by remember { mutableStateOf<List<Person>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDate by remember { mutableStateOf("") } // Input manual (nanti bisa diganti DatePicker)

    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            repo.getBirthdays(uid) { rawList ->
                // LOGIKA SORTING (Mengurutkan dari yang terdekat)
                val sortedList = rawList.sortedBy { person ->
                    val date = DateUtils.parseDate(person.birthDate)
                    if (date != null) {
                        val cal = Calendar.getInstance()
                        cal.time = date
                        // Krik: Ubah tahun lahir jadi tahun sekarang buat perbandingan
                        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))

                        // Kalau tanggalnya sudah lewat tahun ini, anggap tahun depan
                        if (cal.timeInMillis < System.currentTimeMillis()) {
                            cal.add(Calendar.YEAR, 1)
                        }
                        cal.timeInMillis
                    } else {
                        Long.MAX_VALUE // Taruh paling bawah kalau format salah
                    }
                }
                birthdayList = sortedList
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFF81C784)) {
                Icon(Icons.Default.Add, "Tambah")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text("Jadwal Ulang Tahun", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Diurutkan dari yang terdekat", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(birthdayList) { person ->
                    // Hitung umur tahun ini
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val age = DateUtils.getAgeInYear(person.birthDate, currentYear)

                    BirthdayItem(
                        person = person,
                        age = age, // Kirim info umur
                        onDelete = { repo.deleteBirthday(person.id) }
                    )
                }
            }
        }

        // Dialog Tambah (Tetap sama seperti sebelumnya)
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Tambah Teman") },
                text = {
                    Column {
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Nama") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newDate, onValueChange = { newDate = it }, label = { Text("Tanggal (14 Mei 2000)") }, placeholder = { Text("dd MMMM yyyy") })
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
                }
            )
        }
    }
}

// Update Item UI untuk menampilkan Umur
@Composable
fun BirthdayItem(person: Person, age: Int, onDelete: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(person.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Lahir: ${person.birthDate}", fontSize = 12.sp, color = Color.Gray)
                Text("Tahun ini umur: $age", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Hapus", tint = Color.Red)
            }
        }
    }
}