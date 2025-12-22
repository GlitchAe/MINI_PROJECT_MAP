package com.example.miniprojectmap

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Ini harus di file BanaDocViewModel.kt
class BanaDocViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    data class BanaDocUiState(
        val bitmap: Bitmap? = null,
        val selectedUri: Uri? = null,
        val isImageLoading: Boolean = false,
        val isAnalyzing: Boolean = false,
        val diagnosisResult: String? = null,
        val confidenceScore: Int = 0,
        val locationText: String = "Menunggu Lokasi...",
        val lat: Double = 0.0,
        val lng: Double = 0.0
    )

    private val _uiState = MutableStateFlow(BanaDocUiState())
    val uiState: StateFlow<BanaDocUiState> = _uiState.asStateFlow()

    // LOGIC: Load & Resize Image
    fun processImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(isImageLoading = true, selectedUri = uri, diagnosisResult = null)

        viewModelScope.launch(Dispatchers.IO) {
            val resized = ImageUtils.getResizedBitmap(context, uri, 1024)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(bitmap = resized, isImageLoading = false)
            }
        }
    }

    // LOGIC: Ambil Lokasi (GPS)
    @Suppress("MissingPermission")
    fun fetchLocation() {
        viewModelScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    _uiState.value = _uiState.value.copy(
                        lat = location.latitude,
                        lng = location.longitude,
                        locationText = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(locationText = "Lokasi tidak ditemukan (aktifkan GPS)")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(locationText = "Gagal akses lokasi")
            }
        }
    }

    // LOGIC: AI Mock & Save to Firestore
    fun analyzeAndSave() {
        _uiState.value = _uiState.value.copy(isAnalyzing = true)
        fetchLocation()

        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)

            val diseases = listOf("Panama Disease (Layu Fusarium)", "Black Sigatoka", "Banana Bunchy Top", "Sehat / Normal")
            val result = diseases.random()
            val score = Random.nextInt(75, 99)

            val uid = auth.currentUser?.uid
            if (uid != null) {
                val historyData = hashMapOf(
                    "userId" to uid,
                    "diseaseName" to result,
                    "confidence" to score,
                    "date" to SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()),
                    "latitude" to _uiState.value.lat,
                    "longitude" to _uiState.value.lng,
                    "imageUrl" to (_uiState.value.selectedUri?.toString() ?: "")
                )
                db.collection("scan_history").add(historyData).await()
            }

            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                diagnosisResult = result,
                confidenceScore = score
            )
        }
    }
}