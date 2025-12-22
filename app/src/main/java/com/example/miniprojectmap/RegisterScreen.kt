package com.example.miniprojectmap

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) BgDark else BgLight

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

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // --- HEADER GRADASI (SUNSET) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomEnd = 80.dp))
                .background(GradientSunset) // FIX: Warna Baru
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(top = 40.dp, start = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
            }

            Column(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 32.dp, top = 20.dp)
            ) {
                Text("Daftar Akun", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Gabung komunitas kami", fontSize = 16.sp, color = Color.White.copy(0.8f))
            }
        }

        // --- FORM REGISTER ---
        Card(
            modifier = Modifier
                .padding(top = 160.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), visualTransformation = PasswordVisualTransformation(), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Input
                OutlinedTextField(
                    value = birthDate, onValueChange = {}, label = { Text("Tanggal Lahir") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false, readOnly = true, shape = RoundedCornerShape(12.dp),
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Pilih") } },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Daftar (Gradient Sunset)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GradientSunset)
                        .clickable(enabled = !isLoading) {
                            // --- VALIDASI KOMPREHENSIF (NILAI 10) ---
                            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || birthDate.isEmpty()) {
                                Toast.makeText(context, "Mohon isi semua kolom!", Toast.LENGTH_SHORT).show()
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                Toast.makeText(context, "Format Email salah! (Contoh: user@email.com)", Toast.LENGTH_SHORT).show()
                            } else if (password.length < 6) {
                                Toast.makeText(context, "Password terlalu pendek (Min. 6 karakter)", Toast.LENGTH_SHORT).show()
                            } else {
                                // EKSEKUSI REGISTER
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        val userData = hashMapOf("fullName" to fullName, "email" to email, "birthDate" to birthDate, "uid" to userId)
                                        if (userId != null) {
                                            db.collection("users").document(userId).set(userData).addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(context, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                                onRegisterSuccess()
                                            }
                                                // Error handling database (jarang terjadi tapi perlu dijaga)
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    Toast.makeText(context, "Gagal simpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    } else {
                                        // ERROR HANDLING NETWORK / EMAIL SUDAH ADA
                                        isLoading = false
                                        val errorMsg = task.exception?.message ?: "Gagal Mendaftar"
                                        Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("DAFTAR SEKARANG", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}