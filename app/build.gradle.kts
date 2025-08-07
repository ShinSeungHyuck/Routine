plugins {
    id("com.android.application")
    kotlin("android") version "1.9.10"
    kotlin("kapt") version "1.9.10"
}

android {
    namespace = "com.example.routine"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.routine"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xjvm-default=all",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // AndroidX & Material
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.11.0")

    // Compose
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
    implementation("androidx.compose.material3:material3:1.2.0-alpha04")
    implementation("androidx.compose.material:material:1.5.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
}
