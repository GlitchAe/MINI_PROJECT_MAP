package com.example.miniprojectmap

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val currentUser = auth.currentUser

    // State Data User
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) } // Menyimpan Foto Profil

    // State Mode Edit & Loading
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // State Date Picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Launcher untuk Ambil Foto dari Galeri
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) photoUri = uri }
    )

    // AMBIL DATA DARI FIRESTORE SAAT LAYAR DIBUKA
    LaunchedEffect(Unit) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        fullName = document.getString("fullName") ?: ""
                        email = document.getString("email") ?: user.email ?: ""
                        birthDate = document.getString("birthDate") ?: ""
                    }
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
                        birthDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") },
                actions = {
                    IconButton(onClick = { auth.signOut(); onLogout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. FOTO PROFIL (KLIK UNTUK GANTI) ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2F1))
                    .clickable(enabled = isEditing) {
                        // Buka Galeri saat mode Edit
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    // Tampilkan Foto dari Galeri
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto Profil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Tampilkan Icon Default
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF00695C)
                    )
                }

                // Ikon Kamera kecil jika sedang mode edit
                if (isEditing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        Text("Ubah Foto", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TOMBOL EDIT (Pojok Kanan)
            if (!isEditing) {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profil", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. FORM DATA ---

            // Nama Lengkap
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing, // Cuma bisa ngetik kalau lagi mode Edit
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email (Bisa diedit di database, tapi tidak mengubah login auth demi keamanan)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tanggal Lahir (Pakai DatePicker)
            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                label = { Text("Tanggal Lahir") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEditing) { showDatePicker = true }, // Klik untuk buka kalender
                enabled = false,
                readOnly = true,
                trailingIcon = {
                    if(isEditing) IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Black,
                    disabledTrailingIconColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. TOMBOL SIMPAN / BATAL ---
            if (isEditing) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Tombol Batal
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    // Tombol Simpan
                    Button(
                        onClick = {
                            if (fullName.isNotEmpty() && email.isNotEmpty() && birthDate.isNotEmpty() && currentUser != null) {
                                isLoading = true

                                // Data yang mau diupdate ke Firestore
                                val updates = mapOf(
                                    "fullName" to fullName,
                                    "email" to email,
                                    "birthDate" to birthDate
                                    // "photoUri" to photoUri.toString() // Nanti kalau sudah pakai Storage
                                )

                                db.collection("users").document(currentUser.uid)
                                    .update(updates)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        isEditing = false
                                        Toast.makeText(context, "Data Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Simpan")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}