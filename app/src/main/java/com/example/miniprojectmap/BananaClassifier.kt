package com.example.miniprojectmap

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class BananaClassifier(
    private val context: Context,
    private val modelName: String = "best_float16.tflite", // Nama file modelmu
    private val labelName: String = "labels.txt"           // Nama file labelmu
) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    // Standar input size model (Biasanya 224 atau 256. Kita pakai 224 default umum)
    // Jika akurasi jelek, coba ganti ke 256, 300, atau 320 sesuaikan trainingmu.
    private val inputSize = 224

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            // 1. Load Model
            val modelFile = FileUtil.loadMappedFile(context, modelName)
            val options = Interpreter.Options()
            interpreter = Interpreter(modelFile, options)

            // 2. Load Labels
            labels = FileUtil.loadLabels(context, labelName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): Pair<String, Float>? {
        if (interpreter == null) setupInterpreter()

        // 1. Pre-process Gambar (Resize & Normalisasi)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            // .add(NormalizeOp(0f, 255f)) // Aktifkan ini JIKA hasil ngaco (tergantung training)
            .build()

        var tensorImage = TensorImage.fromBitmap(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Siapkan Output Buffer
        // Output biasanya berupa array probabilitas [0.1, 0.8, 0.05, 0.05]
        val outputBuffer = Array(1) { FloatArray(labels.size) }

        // 3. Jalankan Model (Inference)
        interpreter?.run(tensorImage.buffer, outputBuffer)

        // 4. Cari Probabilitas Tertinggi
        val results = outputBuffer[0] // Ambil array hasil
        var maxIndex = -1
        var maxScore = 0f

        for (i in results.indices) {
            if (results[i] > maxScore) {
                maxScore = results[i]
                maxIndex = i
            }
        }

        return if (maxIndex != -1) {
            // Return Nama Penyakit & Confidence Score (0.0 - 1.0)
            Pair(labels[maxIndex], maxScore)
        } else {
            null
        }
    }

    fun close() {
        interpreter?.close()
    }
}