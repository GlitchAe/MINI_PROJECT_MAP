package com.example.miniprojectmap

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit, settingsViewModel: SettingsViewModel = viewModel()) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val currentUser = auth.currentUser

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onBackground

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val currentTheme by settingsViewModel.theme.collectAsState()

    val datePickerState = rememberDatePickerState()

    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) photoUri = uri }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success && tempCameraUri != null) photoUri = tempCameraUri }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // FIX: Gunakan ImageUtils.createImageFile
            val uri = ImageUtils.createImageFile(context);
            tempCameraUri = uri;
            cameraLauncher.launch(uri)
        }
        else Toast.makeText(context, "Izin Kamera Dibutuhkan", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    fullName = document.getString("fullName") ?: ""
                    email = document.getString("email") ?: user.email ?: ""
                    birthDate = document.getString("birthDate") ?: ""
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { millis -> birthDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(millis)) }; showDatePicker = false }) { Text("Pilih") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp)).background(GradientOcean))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Profil Saya", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    IconButton(onClick = { auth.signOut(); onLogout() }, modifier = Modifier.background(Color.White.copy(0.2f), CircleShape)) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.White) }
                }
            }

            Box(modifier = Modifier.offset(y = (-60).dp).align(Alignment.CenterHorizontally)) {
                Box(modifier = Modifier.size(130.dp).shadow(10.dp, CircleShape).clip(CircleShape).background(Color.White).clickable(enabled = isEditing) { showSourceDialog = true }, contentAlignment = Alignment.Center) {
                    if (photoUri != null) AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else Icon(Icons.Default.Person, null, modifier = Modifier.size(70.dp), tint = Color.Gray)
                    if (isEditing) Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.CameraAlt, null, tint = Color.White) }
                }
            }

            Column(modifier = Modifier.offset(y = (-40).dp).padding(horizontal = 24.dp)) {
                if (!isEditing) {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).shadow(6.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(GradientRoyal).clickable { isEditing = true }, contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Edit, null, tint = Color.White); Spacer(modifier = Modifier.width(8.dp)); Text("Edit Profil", fontWeight = FontWeight.Bold, color = Color.White) }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                ProfileInput(label = "Nama Lengkap", value = fullName, enabled = isEditing, textColor = contentColor) { fullName = it }
                ProfileInput(label = "Email", value = email, enabled = isEditing, textColor = contentColor) { email = it }

                OutlinedTextField(
                    value = birthDate, onValueChange = {}, label = { Text("Tanggal Lahir") },
                    modifier = Modifier.fillMaxWidth().clickable(enabled = isEditing) { showDatePicker = true },
                    enabled = false, readOnly = true, shape = RoundedCornerShape(16.dp),
                    trailingIcon = { if(isEditing) IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Pilih") } },
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = contentColor, disabledBorderColor = Color.LightGray, disabledLabelColor = Color.Gray)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(surfaceColor).clickable { showThemeDialog = true }) {
                    Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.SettingsBrightness, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tema Aplikasi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = contentColor)
                            val statusText = when(currentTheme) { AppTheme.SYSTEM -> "Mengikuti Sistem HP"; AppTheme.LIGHT -> "Mode Terang"; AppTheme.DARK -> "Mode Gelap" }
                            Text(statusText, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { isEditing = false }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), shape = RoundedCornerShape(12.dp)) { Text("Batal") }
                        Button(onClick = { if (fullName.isNotEmpty() && email.isNotEmpty() && currentUser != null) { isLoading = true; val updates = mapOf("fullName" to fullName, "email" to email, "birthDate" to birthDate); db.collection("users").document(currentUser.uid).update(updates).addOnSuccessListener { isLoading = false; isEditing = false; Toast.makeText(context, "Berhasil!", Toast.LENGTH_SHORT).show() }.addOnFailureListener { isLoading = false; Toast.makeText(context, "Gagal", Toast.LENGTH_SHORT).show() } } }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp), enabled = !isLoading) { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text("Simpan") }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Pilih Tema") },
            text = { Column { ThemeOption("Ikuti Sistem HP", currentTheme == AppTheme.SYSTEM) { settingsViewModel.setTheme(AppTheme.SYSTEM); showThemeDialog = false }; ThemeOption("Mode Terang", currentTheme == AppTheme.LIGHT) { settingsViewModel.setTheme(AppTheme.LIGHT); showThemeDialog = false }; ThemeOption("Mode Gelap", currentTheme == AppTheme.DARK) { settingsViewModel.setTheme(AppTheme.DARK); showThemeDialog = false } } },
            confirmButton = {}, dismissButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Tutup") } }
        )
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Ubah Foto") },
            text = { Column { ListItem(headlineContent = { Text("Kamera") }, leadingContent = { Icon(Icons.Default.CameraAlt, null) }, modifier = Modifier.clickable { showSourceDialog = false; if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) { val uri = ImageUtils.createImageFile(context); tempCameraUri = uri; cameraLauncher.launch(uri) } else { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) } }); ListItem(headlineContent = { Text("Galeri") }, leadingContent = { Icon(Icons.Default.Image, null) }, modifier = Modifier.clickable { showSourceDialog = false; photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) } },
            confirmButton = {}, dismissButton = { TextButton(onClick = { showSourceDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        if (selected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ProfileInput(label: String, value: String, enabled: Boolean, textColor: Color, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), enabled = enabled, shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, disabledTextColor = textColor))
}