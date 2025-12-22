package com.example.miniprojectmap

import kotlinx.coroutines.delay
import kotlin.random.Random

object BananaClassifier {

    data class Result(
        val diseaseName: String,
        val confidence: String,
        val description: String,
        val solution: String
    )

    private val diseases = listOf(
        Result(
            "Layu Fusarium (Panama)", "96%",
            "Daun menguning dari pinggir, batang pecah vertikal.",
            "Bongkar tanaman, gunakan bibit resisten, sanitasi lahan."
        ),
        Result(
            "Sigatoka (Bercak Daun)", "88%",
            "Bercak memanjang coklat/hitam pada daun.",
            "Potong daun terinfeksi, semprot fungisida, perbaiki drainase."
        ),
        Result(
            "Penyakit Darah (BDB)", "94%",
            "Jantung pisang kering, buah membusuk coklat kemerahan.",
            "Sterilkan alat potong, bungkus jantung pisang, cegah serangga."
        ),
        Result(
            "Tanaman Sehat", "99%",
            "Tidak ditemukan gejala penyakit signifikan.",
            "Lanjutkan perawatan rutin pemupukan dan penyiraman."
        )
    )

    // Simulasi mikir (Mock AI)
    suspend fun classifyImage(): Result {
        delay(1500) // Loading 1.5 detik biar terasa canggih
        return diseases[Random.nextInt(diseases.size)]
    }
}