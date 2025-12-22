package com.example.miniprojectmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BirthdayListScreen(viewModel: BirthdayListViewModel = viewModel()) {
    val birthdayList by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = bgColor,
        floatingActionButton = {
            Box(
                modifier = Modifier.size(60.dp).shadow(10.dp, CircleShape).clip(CircleShape).background(GradientRoyal).clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White, modifier = Modifier.size(30.dp)) }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Daftar Teman", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = contentColor)
            Text("Jangan lupa ucapkan selamat!", fontSize = 16.sp, color = contentColor.copy(0.6f))
            Spacer(modifier = Modifier.height(24.dp))

            if (birthdayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 50.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada data teman.", color = contentColor.copy(0.5f))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(birthdayList) { person ->
                        BirthdayItemCard(person, surfaceColor, contentColor) { viewModel.deleteBirthday(person.id) }
                    }
                }
            }
        }

        if (showDialog) {
            AddBirthdayDialog(onDismiss = { showDialog = false }, onSave = { name, date -> viewModel.addBirthday(name, date); showDialog = false })
        }
    }
}

@Composable
fun BirthdayItemCard(person: Person, surfaceColor: Color, contentColor: Color, onDelete: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(surfaceColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(if (person.isManual) GradientMint else GradientOcean), contentAlignment = Alignment.Center) {
                    val initial = if (person.name.isNotEmpty()) person.name.first().toString().uppercase() else "?"
                    Text(initial, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(person.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = contentColor)
                        if (!person.isManual) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = contentColor.copy(0.5f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(person.birthDate, fontSize = 14.sp, color = contentColor.copy(0.5f))
                    }
                }
            }
            if (person.isManual) {
                IconButton(onClick = onDelete, modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(40.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirthdayDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { millis -> date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(millis)) }; showDatePicker = false }) { Text("Pilih") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Teman") },
        text = { Column { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, shape = RoundedCornerShape(12.dp), singleLine = true); Spacer(modifier = Modifier.height(12.dp)); OutlinedTextField(value = date, onValueChange = { }, label = { Text("Tanggal Lahir") }, placeholder = { Text("Pilih Tanggal") }, readOnly = true, shape = RoundedCornerShape(12.dp), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, null) } }, modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) } },
        confirmButton = { Button(onClick = { if (name.isNotEmpty() && date.isNotEmpty()) onSave(name, date) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}