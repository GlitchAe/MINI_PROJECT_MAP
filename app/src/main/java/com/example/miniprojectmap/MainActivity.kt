package com.example.miniprojectmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Setup Tema (Dark Mode)
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
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // TAMBAHKAN "banadoc_menu" KE DAFTAR SHOW BOTTOM BAR
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

                    // 3. BANADOC AI (Tab Baru di Tengah)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CameraAlt, "AI") },
                        label = { Text("AI Scan") },
                        selected = currentRoute == "banadoc_menu",
                        // Kasih warna beda dikit biar menonjol (Opsional)
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
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
            // --- AUTH & SPLASH ---
            composable("splash") {
                SplashScreen(onNavigateToNext = { if (it) navController.navigate("home") else navController.navigate("login") })
            }
            composable("login") {
                LoginScreen(onLoginSuccess = { navController.navigate("home") }, onNavigateToRegister = { navController.navigate("register") })
            }
            composable("register") {
                RegisterScreen(onRegisterSuccess = { navController.navigate("home") }, onNavigateBack = { navController.popBackStack() })
            }

            // --- MAIN FEATURES ---

            // 1. HOME (Sudah Bersih dari Parameter Scanner/History)
            composable("home") {
                HomeScreen(
                    onNavigateToCalendar = { navController.navigate("calendar") },
                    onNavigateToBirthday = { navController.navigate("birthday_list") }
                )
            }

            // 2. KALENDER
            composable("calendar") { CalendarScreen() }

            // 3. BANADOC MENU (Halaman Menu AI)
            composable("banadoc_menu") {
                BanaDocMenuScreen(
                    onNavigateToScanner = { navController.navigate("banadoc_scanner") },
                    onNavigateToHistory = { navController.navigate("banadoc_history") }
                )
            }

            // 4. BIRTHDAY LIST
            composable("birthday_list") { BirthdayListScreen() }

            // 5. PROFILE
            composable("profile") {
                ProfileScreen(onLogout = { navController.navigate("login") })
            }

            // --- BANADOC SUB-PAGES ---
            composable("banadoc_scanner") {
                ScannerScreen(onScanResult = { path ->
                    val enc = java.net.URLEncoder.encode(path, "UTF-8")
                    navController.navigate("banadoc_result/$enc")
                })
            }
            composable("banadoc_result/{imagePath}") { entry ->
                ScanResultScreen(
                    imagePath = entry.arguments?.getString("imagePath") ?: "",
                    onBackToHome = { navController.navigate("banadoc_menu") { popUpTo("banadoc_menu") { inclusive = true } } }
                )
            }
            composable("banadoc_history") {
                HistoryScreen(onDetailClick = { navController.navigate("banadoc_detail") })
            }
            composable("banadoc_detail") {
                DiseaseDetailScreen()
            }
        }
    }
}