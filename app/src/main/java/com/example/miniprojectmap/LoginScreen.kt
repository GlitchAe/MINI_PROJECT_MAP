package com.example.miniprojectmap

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) BgDark else BgLight // Dari Color.kt baru

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // --- HEADER GRADASI (KURVA) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(bottomStart = 80.dp)) // Kurva estetik
                .background(GradientOcean) // FIX: Pakai Gradasi Baru
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp, bottom = 40.dp)
            ) {
                Text("Halo!", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Silakan Masuk", fontSize = 20.sp, color = Color.White.copy(0.8f))
            }
        }

        // --- FORM LOGIN (FLOATING CARD) ---
        Card(
            modifier = Modifier
                .padding(top = 220.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) SurfaceDark else Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                Spacer(modifier = Modifier.height(24.dp))

                // Input Email
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Input Password
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Tombol Login (Gradient Button Manual)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GradientRoyal)
                        .clickable(enabled = !isLoading) {
                            // --- VALIDASI KETAT (AGAR NILAI 10) ---
                            if (email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(context, "Email dan Password wajib diisi!", Toast.LENGTH_SHORT).show()
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                // Cek format email (harus ada @ dan domain)
                                Toast.makeText(context, "Format Email tidak valid!", Toast.LENGTH_SHORT).show()
                            } else if (password.length < 6) {
                                // Cek panjang password (standar Firebase min 6)
                                Toast.makeText(context, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                            } else {
                                // JIKA LOLOS VALIDASI, BARU EKSEKUSI KE FIREBASE
                                isLoading = true
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        } else {
                                            // ERROR HANDLING (NETWORK/WRONG PASS)
                                            // Menampilkan pesan error asli dari Firebase (misal: "No internet connection")
                                            val errorMsg = task.exception?.message ?: "Terjadi kesalahan"
                                            Toast.makeText(context, "Gagal: $errorMsg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("MASUK", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Link Daftar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Belum punya akun? ", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        "Daftar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0072FF), // Biru Link
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}