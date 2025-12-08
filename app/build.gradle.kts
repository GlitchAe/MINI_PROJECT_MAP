plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.miniprojectmap"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
        compose = true
    }

    defaultConfig {
        applicationId = "com.example.miniprojectmap"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics)

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.firebase.storage)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.glance.appwidget)


    implementation(libs.androidx.navigation.compose.v296)
    implementation(libs.androidx.recyclerview)

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material)

    implementation(libs.guava)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Location (Untuk Lokasi)
    implementation(libs.play.services.location)

    // Ikon Extended (Untuk ikon Scanner/History)
    implementation(libs.androidx.material.icons.extended)

    //retrofit (Hit API)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
}