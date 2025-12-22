package com.example.miniprojectmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    // Fungsi Membuat File Sementara untuk Kamera
    fun createImageFile(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        // Pastikan authority sesuai dengan AndroidManifest (biasanya packageName + .fileprovider)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Fungsi Resize Gambar (Hemat Memori) & Fix Rotasi
    fun getResizedBitmap(context: Context, uri: Uri, maxSize: Int): Bitmap? {
        var inputStream: InputStream? = null
        try {
            // 1. Cek Ukuran Asli
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 2. Hitung Skala
            var scale = 1
            while ((options.outWidth / scale) / 2 >= maxSize && (options.outHeight / scale) / 2 >= maxSize) {
                scale *= 2
            }

            // 3. Load Gambar Kecil
            val options2 = BitmapFactory.Options()
            options2.inSampleSize = scale
            inputStream = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream, null, options2)
            inputStream?.close()

            // 4. Cek Rotasi (Exif)
            if (bitmap != null) {
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val exif = ExifInterface(inputStream)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    inputStream.close()
                }
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}