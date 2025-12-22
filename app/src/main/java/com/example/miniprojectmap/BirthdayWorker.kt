package com.example.miniprojectmap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BirthdayWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            checkBirthdays()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun checkBirthdays() {
        val context = applicationContext
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        // Ambil data teman dari Firestore
        val snapshot = db.collection("users").document(uid).collection("birthdays").get().await()

        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (document in snapshot.documents) {
            val name = document.getString("name") ?: continue
            val birthDateStr = document.getString("birthDate") ?: continue

            try {
                val date = dateFormat.parse(birthDateStr) ?: continue
                val cal = Calendar.getInstance()
                cal.time = date

                // Set tahun ke tahun ini untuk perbandingan
                cal.set(Calendar.YEAR, today.get(Calendar.YEAR))

                // Jika bulan/tanggal sudah lewat di tahun ini, set ke tahun depan (opsional logic)
                // Tapi untuk notifikasi H-0 atau H-1, kita cek day of year nya

                val dayOfYearNow = today.get(Calendar.DAY_OF_YEAR)
                val dayOfYearBday = cal.get(Calendar.DAY_OF_YEAR)

                // Cek HARI INI (H-0)
                if (dayOfYearNow == dayOfYearBday) {
                    showNotification(context, 1, "🎉 Hari ini $name Ulang Tahun!", "Jangan lupa ucapkan selamat ya!")
                }

                // Cek BESOK (H-1)
                if (dayOfYearBday - dayOfYearNow == 1) {
                    showNotification(context, 2, "⏳ Besok $name Ulang Tahun", "Siapkan kado atau ucapan spesial!")
                }

            } catch (e: Exception) {
                Log.e("BirthdayWorker", "Error parsing date", e)
            }
        }
    }

    private fun showNotification(context: Context, notifId: Int, title: String, content: String) {
        val channelId = "birthday_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Buat Channel (Wajib untuk Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pengingat Ulang Tahun", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        // Intent agar kalau notif diklik, buka aplikasi
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar) // Ikon bawaan android
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        manager.notify(notifId + System.currentTimeMillis().toInt(), builder.build())
    }
}