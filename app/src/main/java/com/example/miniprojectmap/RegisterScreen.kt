package com.example.miniprojectmap

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onNavigateBack: () -> Unit) {
    // Tambah state untuk Nama
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val datePickerState = rememberDatePickerState()

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
                title = { Text("Daftar Akun") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Buat Akun Baru", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))

            // INPUT NAMA LENGKAP (BARU)
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = birthDate, onValueChange = {}, label = { Text("Tanggal Lahir") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false, readOnly = true,
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Pilih") } },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && birthDate.isNotEmpty()) {
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                // Simpan Nama juga ke database
                                val userData = hashMapOf(
                                    "fullName" to fullName, // <--- Simpan nama
                                    "email" to email,
                                    "birthDate" to birthDate,
                                    "uid" to userId
                                )
                                if (userId != null) {
                                    db.collection("users").document(userId).set(userData).addOnSuccessListener {
                                        isLoading = false
                                        onRegisterSuccess()
                                    }.addOnFailureListener {
                                        isLoading = false
                                        Toast.makeText(context, "Gagal Simpan Data", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Isi semua kolom!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Daftar Sekarang")
            }
        }
    }
}