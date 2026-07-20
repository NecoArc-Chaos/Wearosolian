plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "dev.solsynth.solian"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.solsynth.solian"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ── BOMs ──
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    val wearComposeBom = platform("androidx.wear.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    implementation(wearComposeBom)

    // ── Wear OS Compose M3 (version from BOM) ──
    implementation("androidx.wear.compose:compose-material3")
    implementation("androidx.wear.compose:compose-foundation")
    implementation("androidx.wear.compose:compose-navigation")

    // ── Core AndroidX (version from BOM where applicable) ──
    implementation("androidx.core:core-ktx")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")

    // ── Networking ──
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── Image Loading ──
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ── Room (SQLite) ──
    val room = "2.6.1"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    // ── WebSocket ──
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
