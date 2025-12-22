package com.example.miniprojectmap

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BanaDocGalleryScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // 1. SOLUSI WARNING: Gunakan rememberCoroutineScope, bukan GlobalScope
    val scope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var analysisResult by remember { mutableStateOf<BananaClassifier.Result?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    // Launcher Galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        analysisResult = null // Reset hasil jika ganti foto
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AREA FOTO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text("Klik untuk Pilih Foto", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TOMBOL ANALISA
        if (selectedImageUri != null && analysisResult == null) {
            Button(
                onClick = {
                    isLoading = true
                    // 2. Menggunakan scope yang aman
                    scope.launch {
                        val result = BananaClassifier.classifyImage()
                        analysisResult = result
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Icon(Icons.Default.Analytics, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analisa Penyakit")
                }
            }
        }

        // HASIL ANALISA
        if (analysisResult != null) {
            val result = analysisResult!!
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hasil Diagnosa:", fontSize = 14.sp, color = Color.Gray)
                    Text(result.diseaseName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text("Akurasi: ${result.confidence}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Solusi:", fontWeight = FontWeight.Bold)
                    Text(result.solution, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TOMBOL SIMPAN
            Button(
                onClick = {
                    isUploading = true
                    val user = FirebaseAuth.getInstance().currentUser

                    if (user == null) {
                        Toast.makeText(context, "Silakan login ulang", Toast.LENGTH_SHORT).show()
                        isUploading = false
                        return@Button
                    }

                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("scan_history/${user.uid}/${System.currentTimeMillis()}.jpg")

                    selectedImageUri?.let { uri ->
                        storageRef.putFile(uri)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                    val data = hashMapOf(
                                        "userId" to user.uid,
                                        "diseaseName" to result.diseaseName,
                                        "confidence" to result.confidence,
                                        "date" to SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                                        "imageUrl" to downloadUrl.toString(),
                                        "description" to result.description
                                    )
                                    FirebaseFirestore.getInstance().collection("scan_history").add(data)
                                        .addOnSuccessListener {
                                            isUploading = false
                                            Toast.makeText(context, "Tersimpan di Riwayat!", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                isUploading = false
                                Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                enabled = !isUploading
            ) {
                if (isUploading) Text("Menyimpan...") else Text("Simpan ke Riwayat")
            }
        }
    }
}