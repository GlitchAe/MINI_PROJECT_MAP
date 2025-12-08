package com.example.miniprojectmap

import android.content.Context
import kotlinx.coroutines.delay
import kotlin.random.Random

// Ini adalah simulasi AI. Nanti diganti dengan TFLite Model.
object BananaClassifier {

    data class Result(
        val diseaseName: String,
        val confidence: String,
        val description: String,
        val solution: String
    )

    // Daftar kemungkinan penyakit pisang (Dummy Data)
    private val diseases = listOf(
        Result(
            "Penyakit Layu Fusarium (Panama)",
            "98%",
            "Daun menguning mulai dari pinggir, batang pecah.",
            "Gunakan bibit bebas penyakit, sanitasi lahan, bongkar tanaman sakit."
        ),
        Result(
            "Penyakit Sigatoka (Bercak Daun)",
            "85%",
            "Bercak kecil memanjang pada daun berwarna coklat/hitam.",
            "Potong daun yang terinfeksi, semprot fungisida, atur drainase."
        ),
        Result(
            "Penyakit Darah (BDB)",
            "92%",
            "Jantung pisang kering, buah busuk berwarna coklat kemerahan.",
            "Sterilkan alat pertanian, bungkus jantung pisang, cegah serangga."
        ),
        Result(
            "Tanaman Sehat",
            "99%",
            "Tidak terdeteksi gejala penyakit pada daun atau batang.",
            "Pertahankan pemupukan dan penyiraman yang baik."
        )
    )

    // Fungsi pura-pura mikir (Analyzing...)
    suspend fun classifyImage(context: Context, imagePath: String): Result {
        delay(2000) // Pura-pura loading 2 detik
        // Mengembalikan hasil acak (Random)
        return diseases[Random.nextInt(diseases.size)]
    }
}