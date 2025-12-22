package com.example.miniprojectmap

import android.Manifest
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val showBottomBar = currentRoute in listOf("home", "calendar", "banadoc_menu", "birthday_list", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(85.dp), contentAlignment = Alignment.BottomCenter) {
                        Box(modifier = Modifier.fillMaxWidth().height(65.dp).shadow(10.dp, RoundedCornerShape(50)).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surface))
                        Row(modifier = Modifier.fillMaxWidth().height(65.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            NavBarIcon(currentRoute == "home", Icons.Filled.Home, Icons.Outlined.Home) { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
                            NavBarIcon(currentRoute == "calendar", Icons.Filled.DateRange, Icons.Outlined.DateRange) { navController.navigate("calendar") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                            NavBarIcon(currentRoute == "banadoc_menu", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt, isSpecial = true) { navController.navigate("banadoc_menu") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                            NavBarIcon(currentRoute == "birthday_list", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List) { navController.navigate("birthday_list") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                            NavBarIcon(currentRoute == "profile", Icons.Filled.Person, Icons.Outlined.Person) { navController.navigate("profile") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "splash", modifier = Modifier.padding(innerPadding)) {
            composable("splash") { SplashScreen { if (it) navController.navigate("home") else navController.navigate("login") } }
            composable("login") { LoginScreen({ navController.navigate("home") }, { navController.navigate("register") }) }
            composable("register") { RegisterScreen({ navController.navigate("home") }, { navController.popBackStack() }) }

            composable("home") { HomeScreen({ navController.navigate("calendar") }, { navController.navigate("birthday_list") }) }
            composable("calendar") { CalendarScreen() }
            composable("birthday_list") { BirthdayListScreen() }
            composable("profile") { ProfileScreen(onLogout = { navController.navigate("login") }, settingsViewModel = settingsViewModel) }

            composable("banadoc_menu") { BanaDocMenuScreen({ navController.navigate("banadoc_gallery") }, { navController.navigate("banadoc_history") }) }

            // FIX: Menggunakan named argument onBack = ... agar tidak ambigu
            composable("banadoc_gallery") { BanaDocGalleryScreen(onBack = { navController.popBackStack() }) }

            composable("banadoc_history") { BanaDocHistoryScreen { navController.popBackStack() } }
        }
    }
}

@Composable
fun NavBarIcon(selected: Boolean, iconFilled: ImageVector, iconOutlined: ImageVector, isSpecial: Boolean = false, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.2f else 1.0f, label = "scale")
    val color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.6f)
    val modifierSpecial = if (isSpecial) Modifier.size(56.dp).offset(y = (-20).dp).shadow(8.dp, CircleShape).background(GradientOcean, CircleShape) else Modifier
    val tint = if (isSpecial) Color.White else color
    Box(modifier = modifierSpecial.width(50.dp).height(50.dp).scale(scale).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }, contentAlignment = Alignment.Center) {
        Icon(imageVector = if (selected) iconFilled else iconOutlined, contentDescription = null, tint = tint, modifier = Modifier.size(if (isSpecial) 28.dp else 26.dp))
    }
}