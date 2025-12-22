package com.example.miniprojectmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    // 1. TAMBAHAN PENTING: Launcher Izin Notifikasi (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Izin diberikan, worker akan jalan
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. TAMBAHAN PENTING: Cek Izin & Start Worker saat Aplikasi Dibuka
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Jalankan Robot Pengecek Ultah
        startBirthdayWorker()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainApp()
                }
            }
        }
    }

    // 3. TAMBAHAN PENTING: Fungsi Menjadwalkan Worker
    private fun startBirthdayWorker() {
        val workRequest = PeriodicWorkRequestBuilder<BirthdayWorker>(
            1, TimeUnit.DAYS // Cek setiap 1 hari
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BirthdayCheck",
            ExistingPeriodicWorkPolicy.KEEP, // Biarkan jadwal lama jalan terus
            workRequest
        )
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("home", "calendar", "banadoc_menu", "birthday_list", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    // 1. HOME
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
                    )

                    // 2. KALENDER
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, "Kalender") },
                        label = { Text("Kalender") },
                        selected = currentRoute == "calendar",
                        onClick = { navController.navigate("calendar") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )

                    // 3. BANADOC (AI SCAN)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CameraAlt, "AI Scan") },
                        label = { Text("BanaDoc") },
                        selected = currentRoute == "banadoc_menu",
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary),
                        onClick = { navController.navigate("banadoc_menu") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )

                    // 4. ULTAH
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, "Ultah") },
                        label = { Text("Ultah") },
                        selected = currentRoute == "birthday_list",
                        onClick = { navController.navigate("birthday_list") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )

                    // 5. PROFIL
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, "Profil") },
                        label = { Text("Profil") },
                        selected = currentRoute == "profile",
                        onClick = { navController.navigate("profile") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            // AUTH
            composable("splash") { SplashScreen(onNavigateToNext = { if (it) navController.navigate("home") else navController.navigate("login") }) }
            composable("login") { LoginScreen(onLoginSuccess = { navController.navigate("home") }, onNavigateToRegister = { navController.navigate("register") }) }
            composable("register") { RegisterScreen(onRegisterSuccess = { navController.navigate("home") }, onNavigateBack = { navController.popBackStack() }) }

            // FITUR
            composable("home") {
                HomeScreen(
                    onNavigateToCalendar = { navController.navigate("calendar") },
                    onNavigateToBirthday = { navController.navigate("birthday_list") }
                )
            }
            composable("calendar") { CalendarScreen() }
            composable("birthday_list") { BirthdayListScreen() }
            composable("profile") { ProfileScreen(onLogout = { navController.navigate("login") }) }

            // BANADOC
            composable("banadoc_menu") {
                BanaDocMenuScreen(
                    onNavigateToGallery = { navController.navigate("banadoc_gallery") },
                    onNavigateToHistory = { navController.navigate("banadoc_history") }
                )
            }
            composable("banadoc_gallery") {
                BanaDocGalleryScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("banadoc_history") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Riwayat Scan (Segera Hadir)")
                }
            }
        }
    }
}