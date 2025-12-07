package com.example.miniprojectmap

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val FORMAT_DATE = "dd MMMM yyyy"

    fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat(FORMAT_DATE, Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    // Fungsi Baru: Menghitung Tanggal Ulang Tahun Berikutnya
    fun getNextBirthday(birthDateString: String): Date? {
        val birthDate = parseDate(birthDateString) ?: return null

        val today = Calendar.getInstance()
        // Reset jam hari ini ke 00:00:00 agar adil
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val nextBirthday = Calendar.getInstance()
        nextBirthday.time = birthDate
        nextBirthday.set(Calendar.YEAR, today.get(Calendar.YEAR))

        // Jika ulang tahun tahun ini SUDAH LEWAT (kemarin), maka ulang tahun berikutnya tahun depan
        // PENTING: Jika hari ini ultah, !before akan True, jadi tetap tahun ini (benar)
        if (nextBirthday.before(today)) {
            nextBirthday.add(Calendar.YEAR, 1)
        }

        return nextBirthday.time
    }

    // Fungsi Baru: Menghitung Umur pada Ulang Tahun Berikutnya
    fun getAgeOnNextBirthday(birthDateString: String): Int {
        val birthDate = parseDate(birthDateString) ?: return 0
        val nextBirthday = getNextBirthday(birthDateString) ?: return 0

        val birthCal = Calendar.getInstance().apply { time = birthDate }
        val nextCal = Calendar.getInstance().apply { time = nextBirthday }

        return nextCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
    }

    // Cek apakah tanggal di kalender == ulang tahun (abaikan tahun)
    fun isBirthdayOnDate(birthDateString: String, selectedYear: Int, selectedMonth: Int, selectedDay: Int): Boolean {
        val birthDate = parseDate(birthDateString) ?: return false
        val cal = Calendar.getInstance().apply { time = birthDate }
        return cal.get(Calendar.DAY_OF_MONTH) == selectedDay && cal.get(Calendar.MONTH) == selectedMonth
    }

    // Hitung umur simpel untuk kalender
    fun getAgeInYear(birthDateString: String, targetYear: Int): Int {
        val birthDate = parseDate(birthDateString) ?: return 0
        val cal = Calendar.getInstance().apply { time = birthDate }
        return targetYear - cal.get(Calendar.YEAR)
    }

    fun getZodiac(birthDateString: String): String {
        val date = parseDate(birthDateString) ?: return ""
        val cal = Calendar.getInstance()
        cal.time = date
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) // Januari = 0

        return when (month) {
            0 -> if (day < 20) "♑ Capricorn" else "♒ Aquarius"      // Jan
            1 -> if (day < 19) "♒ Aquarius" else "♓ Pisces"        // Feb
            2 -> if (day < 21) "♓ Pisces" else "♈ Aries"          // Mar
            3 -> if (day < 20) "♈ Aries" else "♉ Taurus"          // Apr
            4 -> if (day < 21) "♉ Taurus" else "♊ Gemini"         // Mei
            5 -> if (day < 21) "♊ Gemini" else "♋ Cancer"         // Jun
            6 -> if (day < 23) "♋ Cancer" else "♌ Leo"           // Jul
            7 -> if (day < 23) "♌ Leo" else "♍ Virgo"            // Agu
            8 -> if (day < 23) "♍ Virgo" else "♎ Libra"          // Sep
            9 -> if (day < 23) "♎ Libra" else "♏ Scorpio"        // Okt
            10 -> if (day < 22) "♏ Scorpio" else "♐ Sagittarius" // Nov
            11 -> if (day < 22) "♐ Sagittarius" else "♑ Capricorn" // Des
            else -> ""
        }
    }
}