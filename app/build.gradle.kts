plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11)) // 설치된 버전으로 조정
        }
    }

    kotlin {
        jvmToolchain(11)
    }

    //kotlinOptions {
    //    jvmTarget = "1.8"
    //}

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    //implementation("androidx.compose.compiler:compiler:1.4.7")
    //implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")
    testImplementation("junit:junit:4.13.2")
    ksp("androidx.room:room-compiler:2.5.1")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.room:room-ktx:2.5.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.room:room-runtime:2.5.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.google.firebase:firebase-analytics")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    implementation("androidx.activity:activity-compose:1.8.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.3")
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
