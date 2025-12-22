package com.example.miniprojectmap

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanaDocGalleryScreen(
    onBack: () -> Unit,
    viewModel: BanaDocViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.processImage(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) viewModel.processImage(tempCameraUri!!)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) viewModel.fetchLocation()
        else Toast.makeText(context, "Izin Lokasi dibutuhkan", Toast.LENGTH_SHORT).show()
    }

    fun checkPermissionAndAnalyze() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.analyzeAndSave()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Scan & Diagnosa", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(20.dp)).background(surfaceColor),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isImageLoading) {
                    CircularProgressIndicator()
                } else if (uiState.bitmap != null) {
                    Image(bitmap = uiState.bitmap!!.asImageBitmap(), contentDescription = "Preview", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Image, null, modifier = Modifier.size(60.dp), tint = Color.Gray); Text("Belum ada foto", color = Color.Gray) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        tempCameraUri = ImageUtils.createImageFile(context)
                        cameraLauncher.launch(tempCameraUri!!)
                    },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                ) { Icon(Icons.Default.CameraAlt, null); Spacer(modifier = Modifier.width(8.dp)); Text("Kamera") }

                OutlinedButton(
                    onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                ) { Icon(Icons.Default.Image, null); Spacer(modifier = Modifier.width(8.dp)); Text("Galeri") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { checkPermissionAndAnalyze() },
                enabled = uiState.selectedUri != null && !uiState.isAnalyzing && !uiState.isImageLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isAnalyzing) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(12.dp)); Text("Menganalisis AI...") } else { Text("Diagnosa Sekarang") }
            }

            if (uiState.diagnosisResult != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (uiState.diagnosisResult!!.contains("Sehat")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (uiState.diagnosisResult!!.contains("Sehat")) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if (uiState.diagnosisResult!!.contains("Sehat")) Color(0xFF2E7D32) else Color(0xFFC62828)); Spacer(modifier = Modifier.width(12.dp)); Text("Hasil Diagnosa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black) }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(uiState.diagnosisResult!!, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Text("Akurasi AI: ${uiState.confidenceScore}%", fontSize = 14.sp, color = Color.Gray)

                        // FIX: Ganti Divider -> HorizontalDivider
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(uiState.locationText, fontSize = 12.sp, color = Color.Gray) }
                    }
                }
            }
        }
    }
}