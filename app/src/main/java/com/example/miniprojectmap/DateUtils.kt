package com.example.miniprojectmap

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    // Format tanggal yang kita simpan di Firestore (contoh: "14 Mei 1995")
    private const val FORMAT_DATE = "dd MMMM yyyy"

    // Fungsi untuk cek apakah Tanggal & Bulan sama (Mengabaikan Tahun)
    fun isBirthdayOnDate(birthDateString: String, selectedYear: Int, selectedMonth: Int, selectedDay: Int): Boolean {
        return try {
            val formatter = SimpleDateFormat(FORMAT_DATE, Locale.getDefault())
            val date = formatter.parse(birthDateString) ?: return false

            val calendar = Calendar.getInstance()
            calendar.time = date

            // Ambil tanggal & bulan dari data lahir
            val birthDay = calendar.get(Calendar.DAY_OF_MONTH)
            val birthMonth = calendar.get(Calendar.MONTH) // Ingat: Januari = 0

            // Bandingkan dengan tanggal yang dipilih di Kalender
            // (Kita abaikan tahun lahirnya, yang penting tanggal & bulannya cocok)
            return birthDay == selectedDay && birthMonth == selectedMonth
        } catch (e: Exception) {
            false
        }
    }

    // Fungsi untuk menghitung umur di tahun tertentu
    fun getAgeInYear(birthDateString: String, targetYear: Int): Int {
        return try {
            val formatter = SimpleDateFormat(FORMAT_DATE, Locale.getDefault())
            val date = formatter.parse(birthDateString) ?: return 0
            val calendar = Calendar.getInstance()
            calendar.time = date

            val birthYear = calendar.get(Calendar.YEAR)
            return targetYear - birthYear
        } catch (e: Exception) {
            0
        }
    }

    // Fungsi mengubah string jadi objek Date untuk keperluan sorting
    fun parseDate(dateString: String): Date? {
        return try {
            val formatter = SimpleDateFormat(FORMAT_DATE, Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}