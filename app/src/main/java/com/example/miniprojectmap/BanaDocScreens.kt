package com.example.miniprojectmap

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. HALAMAN SCANNER (KAMERA ASLI)
@Composable
fun ScannerScreen(
    onScanResult: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State Izin Kamera
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            val previewView = remember { PreviewView(context) }
            var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

            // Setup CameraX
            LaunchedEffect(Unit) {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraX", "Gagal bind kamera", exc)
                    }
                }, ContextCompat.getMainExecutor(context))
            }

            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

            IconButton(
                onClick = {
                    val photoFile = FileUtils.CreateImageFile(context)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val savedUri = Uri.fromFile(photoFile)
                                onScanResult(savedUri.toString())
                            }

                            override fun onError(exc: ImageCaptureException) {
                                Log.e("CameraX", "Gagal foto: ${exc.message}", exc)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .border(4.dp, Color(0xFFFBC02D), CircleShape)
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Foto", tint = Color.Black)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Izin kamera diperlukan untuk fitur ini.")
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Izinkan Kamera")
            }
        }
    }
}

// 2. HALAMAN HASIL SCAN (UPDATE BESAR - FITUR AI & UPLOAD)
@Composable
fun ScanResultScreen(
    imagePath: String,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    // State untuk UI
    var analysisResult by remember { mutableStateOf<BananaClassifier.Result?>(null) }
    var isAnalyzing by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }

    // Efek Samping: Jalankan Analisa saat layar dibuka
    LaunchedEffect(Unit) {
        // Panggil "Otak" AI kita
        analysisResult = BananaClassifier.classifyImage(context, imagePath)
        isAnalyzing = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Gambar Background (Full)
        Image(
            painter = rememberAsyncImagePainter(imagePath),
            contentDescription = "Hasil Foto",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay Gelap biar tulisan terbaca
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        // 2. Konten Hasil (Di Tengah/Bawah)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indikator Loading Analisa
            if (isAnalyzing) {
                CircularProgressIndicator(color = Color(0xFFFBC02D))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sedang menganalisa pisang...", color = Color.Gray)
            }
            else if (analysisResult != null) {
                // HASIL DETEKSI
                val result = analysisResult!!

                Text("Hasil Deteksi:", fontSize = 14.sp, color = Color.Gray)
                Text(result.diseaseName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))

                // Badge Akurasi
                Surface(
                    color = if(result.diseaseName.contains("Sehat")) Color(0xFFC8E6C9) else Color(0xFFFFCCBC),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Akurasi: ${result.confidence}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail & Solusi
                Text("Gejala:", fontWeight = FontWeight.Bold)
                Text(result.description, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Saran Penanganan:", fontWeight = FontWeight.Bold)
                Text(result.solution, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(24.dp))

                // TOMBOL SIMPAN
                Button(
                    onClick = {
                        isUploading = true
                        val file = Uri.fromFile(File(Uri.parse(imagePath).path))
                        // Upload ke Folder: scan_history/UID_USER/WAKTU.jpg
                        val storageRef = storage.reference.child("scan_history/${auth.currentUser?.uid}/${System.currentTimeMillis()}.jpg")

                        // 1. Upload Foto ke Storage
                        storageRef.putFile(file)
                            .addOnSuccessListener {
                                // 2. Ambil URL Foto
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    // 3. Simpan Data ke Firestore
                                    val historyData = hashMapOf(
                                        "userId" to auth.currentUser?.uid,
                                        "diseaseName" to result.diseaseName,
                                        "confidence" to result.confidence,
                                        "date" to SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()),
                                        "imageUrl" to uri.toString(),
                                        "description" to result.description
                                    )

                                    db.collection("scan_history")
                                        .add(historyData)
                                        .addOnSuccessListener {
                                            isUploading = false
                                            Toast.makeText(context, "Disimpan ke Riwayat!", Toast.LENGTH_SHORT).show()
                                            onBackToHome()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                isUploading = false
                                Toast.makeText(context, "Gagal Upload: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D))
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menyimpan...")
                    } else {
                        Text("Simpan ke Riwayat", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Batal
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup Tanpa Simpan")
                }
            }
        }
    }
}

// 3. HALAMAN RIWAYAT (Placeholder - Akan kita update setelah ini)
@Composable
fun HistoryScreen(onDetailClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("List Riwayat Penyakit")
            Button(onClick = onDetailClick) {
                Text("Lihat Detail (Contoh)")
            }
        }
    }
}

// 4. HALAMAN DETAIL (Placeholder)
@Composable
fun DiseaseDetailScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Detail Lengkap Penyakit")
    }
}