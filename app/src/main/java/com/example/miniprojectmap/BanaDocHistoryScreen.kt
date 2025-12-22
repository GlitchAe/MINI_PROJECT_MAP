package com.example.miniprojectmap

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data Class Khusus untuk History
data class ScanHistory(
    val id: String,
    val diseaseName: String,
    val confidence: Int,
    val date: String,
    val imageUrl: String,
    val lat: Double,
    val lng: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanaDocHistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // State untuk menampung list history
    var historyList by remember { mutableStateOf<List<ScanHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Efek untuk mengambil data Realtime dari Firestore
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("scan_history")
                .whereEqualTo("userId", uid) // Filter punya user sendiri
                // .orderBy("date", Query.Direction.DESCENDING) // Hati-hati, format tanggal String tidak bisa di-sort query
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Toast.makeText(context, "Gagal ambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val items = snapshots.documents.mapNotNull { doc ->
                            try {
                                // --- BAGIAN PENTING: SAFE PARSING (ANTI CRASH) ---
                                ScanHistory(
                                    id = doc.id,
                                    diseaseName = doc.getString("diseaseName") ?: "Tidak diketahui",
                                    // Firestore kadang membaca Int sebagai Long, jadi harus di-cast aman
                                    confidence = (doc.get("confidence") as? Number)?.toInt() ?: 0,
                                    date = doc.getString("date") ?: "-",
                                    imageUrl = doc.getString("imageUrl") ?: "",
                                    // Handle jika data lama tidak punya GPS
                                    lat = (doc.get("latitude") as? Number)?.toDouble() ?: 0.0,
                                    lng = (doc.get("longitude") as? Number)?.toDouble() ?: 0.0
                                )
                            } catch (e: Exception) {
                                null // Jika ada data rusak, skip saja (jangan crash)
                            }
                        }.sortedByDescending { it.date } // Sort manual di aplikasi berdasarkan tanggal string (opsional)

                        historyList = items
                        isLoading = false
                    }
                }
        } else {
            isLoading = false
        }
    }

    // Fungsi Hapus History
    fun deleteHistory(docId: String) {
        db.collection("scan_history").document(docId).delete()
            .addOnSuccessListener { Toast.makeText(context, "Dihapus", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Diagnosa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(60.dp))
                        Text("Belum ada riwayat scan.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(historyList) { item ->
                        HistoryCard(item, onDelete = { deleteHistory(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: ScanHistory, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.diseaseName.contains("Sehat")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gambar (Thumbnail)
            if (item.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(70.dp).background(Color.LightGray, RoundedCornerShape(12.dp)))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Teks
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.diseaseName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Akurasi: ${item.confidence}%",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // Info GPS Kecil
                if (item.lat != 0.0 || item.lng != 0.0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Text(
                            text = "${item.lat}, ${item.lng}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Tombol Hapus
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red.copy(0.6f))
            }
        }
    }
}