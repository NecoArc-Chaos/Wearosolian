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
    // ── Compose BOM ──
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))

    // ── Wear OS Compose (foundation/navigation from BOM, M3 is alpha) ──
    implementation("androidx.wear.compose:compose-foundation")
    implementation("androidx.wear.compose:compose-navigation")
    implementation("androidx.wear.compose:compose-material3:1.0.0-alpha23")

    // ── Core AndroidX ──
    implementation("androidx.activity:activity-compose")
    implementation("androidx.core:core-ktx")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")

    // ── Wear OS base ──
    implementation("androidx.wear:wear:1.3.0")

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
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
