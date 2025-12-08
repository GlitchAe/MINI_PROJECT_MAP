package com.example.miniprojectmap

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    // Fungsi membuat file gambar kosong dengan nama unik (berdasarkan waktu)
    fun CreateImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        // Simpan di folder gambar privat aplikasi (Internal Storage)
        // Orang lain/aplikasi lain tidak bisa lihat file ini (Aman!)
        val storageDir = context.filesDir

        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
}