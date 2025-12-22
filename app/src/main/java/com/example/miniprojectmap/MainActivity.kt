package com.example.miniprojectmap

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Izin Notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        startBirthdayWorker()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val selectedTheme by settingsViewModel.theme.collectAsState()
            val isSystemDark = isSystemInDarkTheme()

            val useDarkTheme = when (selectedTheme) {
                AppTheme.SYSTEM -> isSystemDark
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            // Fix Status Bar (Warna & Ikon)
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    @Suppress("DEPRECATION")
                    window.statusBarColor = if (useDarkTheme) BgDark.toArgb() else BgLight.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
                }
            }

            val darkColors = darkColorScheme(primary = Color(0xFFD0BCFF), background = BgDark, surface = SurfaceDark, onBackground = Color.White, onSurface = Color.White)
            val lightColors = lightColorScheme(primary = Color(0xFF6650a4), background = BgLight, surface = Color.White, onBackground = Color.Black, onSurface = Color.Black)

            MaterialTheme(colorScheme = if (useDarkTheme) darkColors else lightColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainApp(settingsViewModel)
                }
            }
        }
    }

    private fun startBirthdayWorker() {
        val workRequest = PeriodicWorkRequestBuilder<BirthdayWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("BirthdayCheck", ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }
}

@Composable
fun MainApp(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tentukan kapan Bottom Bar muncul
    val showBottomBar = currentRoute in listOf("home", "calendar", "banadoc_menu", "birthday_list", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MyBottomNavigation(currentRoute, navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. SPLASH SCREEN
            composable("splash") {
                SplashScreen { isLoggedIn ->
                    if (isLoggedIn) {
                        // Jika sudah login, ke Home & HAPUS splash dari backstack
                        navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                    } else {
                        // Jika belum, ke Login & HAPUS splash
                        navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                    }
                }
            }

            // 2. AUTHENTICATION
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        // Login Sukses -> Ke Home -> HAPUS SEMUA history login sebelumnya
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true } // 0 artinya hapus sampai akar
                        }
                    },
                    onRegisterClick = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        // Register Sukses -> Ke Home -> HAPUS SEMUA history
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLoginClick = { navController.popBackStack() } // Balik ke Login
                )
            }

            // 3. MAIN MENU (Bottom Bar Items)
            composable("home") {
                HomeScreen(
                    onNavigateToCalendar = { navController.navigateSingleTopTo("calendar") },
                    onNavigateToBirthday = { navController.navigateSingleTopTo("birthday_list") }
                )
            }
            composable("calendar") { CalendarScreen() }
            composable("birthday_list") { BirthdayListScreen() }
            composable("profile") {
                ProfileScreen(
                    onLogout = {
                        // Logout -> Ke Login -> HAPUS SEMUA history home
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    settingsViewModel = settingsViewModel
                )
            }

            // 4. BANADOC FEATURES
            composable("banadoc_menu") {
                BanaDocMenuScreen(
                    onScanClick = { navController.navigate("banadoc_gallery") },
                    onHistoryClick = { navController.navigate("banadoc_history") }
                )
            }

            // Sub-menu BanaDoc (Pakai tombol back bawaan HP normal)
            composable("banadoc_gallery") { BanaDocGalleryScreen(onBack = { navController.popBackStack() }) }
            composable("banadoc_history") { BanaDocHistoryScreen { navController.popBackStack() } }
        }
    }
}

// --- KOMPONEN NAVIGASI YANG RAPI ---

@Composable
fun MyBottomNavigation(currentRoute: String?, navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(85.dp), contentAlignment = Alignment.BottomCenter) {
            // Background Putih Melengkung
            Box(modifier = Modifier.fillMaxWidth().height(65.dp).shadow(10.dp, RoundedCornerShape(50)).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surface))

            // Ikon-ikon
            Row(modifier = Modifier.fillMaxWidth().height(65.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                NavBarIcon(currentRoute == "home", Icons.Filled.Home, Icons.Outlined.Home) {
                    navController.navigateSingleTopTo("home")
                }
                NavBarIcon(currentRoute == "calendar", Icons.Filled.DateRange, Icons.Outlined.DateRange) {
                    navController.navigateSingleTopTo("calendar")
                }
                // Ikon Tengah Besar
                NavBarIcon(currentRoute == "banadoc_menu", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt, isSpecial = true) {
                    navController.navigateSingleTopTo("banadoc_menu")
                }
                NavBarIcon(currentRoute == "birthday_list", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List) {
                    navController.navigateSingleTopTo("birthday_list")
                }
                NavBarIcon(currentRoute == "profile", Icons.Filled.Person, Icons.Outlined.Person) {
                    navController.navigateSingleTopTo("profile")
                }
            }
        }
    }
}

// Fungsi Ekstensi Sakti: Navigasi Tanpa Tumpukan Sampah
fun NavHostController.navigateSingleTopTo(route: String) {
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) {
            saveState = true // Simpan state halaman sebelumnya (misal isian form)
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

@Composable
fun NavBarIcon(selected: Boolean, iconFilled: ImageVector, iconOutlined: ImageVector, isSpecial: Boolean = false, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.2f else 1.0f, label = "scale")
    val color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.6f)
    val modifierSpecial = if (isSpecial) Modifier.size(56.dp).offset(y = (-20).dp).shadow(8.dp, CircleShape).background(GradientOcean, CircleShape) else Modifier
    val tint = if (isSpecial) Color.White else color

    Box(
        modifier = modifierSpecial.width(50.dp).height(50.dp).scale(scale)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = if (selected) iconFilled else iconOutlined, contentDescription = null, tint = tint, modifier = Modifier.size(if (isSpecial) 28.dp else 26.dp))
    }
}