package com.example.miniprojectmap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// 1. HALAMAN SCANNER (Kamera)
@Composable
fun ScannerScreen(onScanResult: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Layar Kamera (Akan dibuat)")
            Button(onClick = onScanResult) {
                Text("Simulasi Jepret Foto")
            }
        }
    }
}

// 2. HALAMAN HASIL SCAN
@Composable
fun ScanResultScreen(onBackToHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hasil Analisa Machine Learning")
            Button(onClick = onBackToHome) {
                Text("Selesai")
            }
        }
    }
}

// 3. HALAMAN RIWAYAT
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

// 4. HALAMAN DETAIL PENYAKIT
@Composable
fun DiseaseDetailScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Detail Lengkap Penyakit")
    }
}