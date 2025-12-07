package com.example.miniprojectmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToBirthday: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("Loading...") }
    val userEmail = auth.currentUser?.email ?: ""

    // Ambil data nama (Refresh setiap kali layar tampil)
    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userName = document.getString("fullName") ?: "User"
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // KARTU 1: WELCOME
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(50.dp), tint = Color(0xFF006064))
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text("Welcome,", fontSize = 14.sp, color = Color(0xFF006064))
                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF006064))
                        Text(userEmail, fontSize = 12.sp, color = Color(0xFF00838F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // KARTU 2: NEAREST BIRTHDAY
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF81D4FA)), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(50.dp), tint = Color(0xFF01579B))
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text("Nearest Birthday", fontSize = 12.sp, color = Color(0xFF01579B))
                        Text("Ferry Irwandi", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF01579B))
                        Text("24 June 2026", color = Color(0xFF0277BD))
                        Text("21 Years Old", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color(0xFF0277BD))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            MenuButton("See the Calendar", Icons.Default.DateRange, onNavigateToCalendar, Color(0xFF81C784))
            Spacer(modifier = Modifier.height(12.dp))
            MenuButton("Birthday List", Icons.AutoMirrored.Filled.List, onNavigateToBirthday, Color(0xFF81C784))
        }
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit, color: Color) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = color), shape = MaterialTheme.shapes.small) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontSize = 16.sp, color = Color.Black)
            Icon(icon, null, tint = Color.Black)
        }
    }
}