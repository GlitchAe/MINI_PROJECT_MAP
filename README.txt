=============================================================================
                  	HUT COMMUNITY TRACKER APP
					X
			BANADOC - BANANA DISEASE DETECTION ML
                             UAS MOBILE PROGRAMMING
=============================================================================

[ INFO MAHASISWA ]
Nama    : Devin Nathanael
NIM     : 00000103231
Kelas   : IF570-C/CL

-----------------------------------------------------------------------------
1. DESKRIPSI APLIKASI
-----------------------------------------------------------------------------
HUT Community Tracker dirancang untuk mempermudah pencatatan data ulang tahun, memberikan notifikasi otomatis, 
serta memastikan setiap anggota tetap merasa dihargai. Dengan adanya sistem yang terpusat dan mudah digunakan, 
komunitas dapat menjaga kehangatan internal, meningkatkan partisipasi, dan memperkuat solidaritas antaranggota.

BanaDoc AI dirancang untuk membantu petani  mendeteksi penyakit pada tanaman pisang melalui citra daun. 
Aplikasi ini mengintegrasikan fitur native Android (Kamera & GPS) dengan layanan Cloud (Firebase) untuk 
penyimpanan data secara realtime.

-----------------------------------------------------------------------------
2. FITUR UNGGULAN (CORE FEATURES)
-----------------------------------------------------------------------------
A. Native Features (Nilai Plus):
   1. Kamera & Galeri: Mendukung pengambilan gambar resolusi tinggi dengan 
      teknik "Bitmap Downsampling" untuk efisiensi memori (Anti-Crash).
   2. Geo-Tagging (GPS): Menggunakan FusedLocationProviderClient untuk 
      menandai lokasi pengambilan sampel penyakit secara akurat dan hemat baterai.

B. Cloud Features (Firebase):
   1. Authentication: Login dan Register menggunakan Email & Password.
   2. Firestore Database: Menyimpan riwayat scan (foto, hasil, lokasi, tanggal) 
      dan data profil pengguna secara realtime.

C. Fitur Tambahan:
   1. Birthday List: Manajemen daftar ulang tahun teman (CRUD).
   2. Dark Mode Support: Tampilan otomatis menyesuaikan tema HP.

-----------------------------------------------------------------------------
3. TEKNOLOGI YANG DIGUNAKAN
-----------------------------------------------------------------------------
- Bahasa Pemrograman : Kotlin
- UI Framework       : Jetpack Compose (Material Design 3)
- Architecture       : MVVM (Model-View-ViewModel) - Strict Pattern
- Asynchronous       : Kotlin Coroutines & Flow
- Backend            : Firebase Authentication & Firestore
- Image Loading      : Coil & Native Bitmap Factory
- Navigation         : Jetpack Navigation Compose

-----------------------------------------------------------------------------
4. CARA INSTALASI & MENJALANKAN (SETUP)
-----------------------------------------------------------------------------
PENTING: Aplikasi ini membutuhkan koneksi ke Firebase agar berjalan normal.

Langkah-langkah:
1. Ekstrak file project (Zip).
2. Buka Android Studio -> File -> Open -> Pilih folder project.
3. [WAJIB] Pastikan file 'google-services.json' sudah ada di dalam 
   folder 'app/'. Jika belum ada, aplikasi akan error saat build.
4. Biarkan Gradle melakukan sinkronisasi (Sync Project with Gradle Files).
5. Jalankan aplikasi (Run 'app') menggunakan Emulator (Pixel 6/7 API 34) 
   atau Device Fisik.

Catatan untuk Penguji:
- Saat pertama kali fitur Scan digunakan, aplikasi akan meminta Izin Kamera 
  dan Izin Lokasi. Harap pilih "Allow/Izinkan" agar fitur berjalan.

-----------------------------------------------------------------------------
5. CATATAN PENGEMBANG (DEV NOTES)
-----------------------------------------------------------------------------
- Machine Learning: 
  Untuk keperluan stabilitas demo UAS, saat ini modul diagnosa menggunakan 
  Simulasi Logika (Mock AI) yang berjalan secara Asynchronous di background thread.
  Struktur kode sudah disiapkan untuk integrasi TFLite (file model tersedia 
  di folder assets/best_float16.tflite) namun dinonaktifkan sementara untuk 
  memastikan aplikasi berjalan ringan di semua device penguji.

- Navigasi:
  Menggunakan Single-Activity Architecture dengan manajemen Back Stack yang 
  sudah dioptimalkan (tidak ada loop login/home).

-----------------------------------------------------------------------------
Terima kasih.
=============================================================================
