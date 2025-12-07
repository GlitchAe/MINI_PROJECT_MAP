package com.example.miniprojectmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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

    // Tentukan halaman mana yang punya Bottom Navbar
    val showBottomBar = currentRoute in listOf("home", "calendar", "birthday_list", "profile")

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
                    // 3. ULTAH
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, "Ultah") },
                        label = { Text("Ultah") },
                        selected = currentRoute == "birthday_list",
                        onClick = { navController.navigate("birthday_list") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )
                    // 4. PROFIL
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
                SplashScreen(onNavigateToNext = { isLoggedIn ->
                    if (isLoggedIn) navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                    else navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                })
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- MAIN FEATURES ---
            composable("home") {
                HomeScreen(
                    onNavigateToCalendar = { navController.navigate("calendar") },
                    onNavigateToBirthday = { navController.navigate("birthday_list") },
                    // ▼▼▼ JANGAN LUPA DUA BARIS INI (BANADOC) ▼▼▼
                    onNavigateToScanner = { navController.navigate("banadoc_scanner") },
                    onNavigateToHistory = { navController.navigate("banadoc_history") }
                )
            }
            composable("calendar") { CalendarScreen() }
            composable("birthday_list") { BirthdayListScreen() }

            composable("profile") {
                ProfileScreen(
                    onLogout = {
                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                    }
                )
            }

            // --- BANADOC FEATURES (MACHINE LEARNING) ---
            composable("banadoc_scanner") {
                ScannerScreen(
                    onScanResult = { navController.navigate("banadoc_result") }
                )
            }

            composable("banadoc_result") {
                ScanResultScreen(
                    onBackToHome = {
                        navController.navigate("home") { popUpTo("home") { inclusive = true } }
                    }
                )
            }

            composable("banadoc_history") {
                HistoryScreen(
                    onDetailClick = { navController.navigate("banadoc_detail") }
                )
            }

            composable("banadoc_detail") {
                DiseaseDetailScreen()
            }
        }
    }
}