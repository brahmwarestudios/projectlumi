plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    // Use the alias if defined in your libs.versions.toml,
    // otherwise this version matches your project's Kotlin version
    kotlin("plugin.serialization") version "2.3.21"
}

android {
    namespace = "com.brahmware.lumi_alpha"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.brahmware.lumi_alpha"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"

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
        // Upgraded to Java 17 as it is the standard for AGP 8.x+ and Android 16
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Platform Libraries from Version Catalog
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.guava)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase - Updated to latest May 2026 BoM
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // AR & SceneView
    // ARCore 1.51.0+ is required for full API 36 (Android 16) support
    implementation("com.google.ar:core:1.52.0")
    // SceneView 2.2.1 remains the stable branch for ARSceneView integration
    implementation("io.github.sceneview:arsceneview:2.2.1")

    // ML Kit Pose Detection - Updated to 18.0.0-beta5 stable
    implementation("com.google.mlkit:pose-detection:18.0.0-beta5")
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta5")

    // CameraX - Updated to 1.4.0 stable
    val cameraxVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Supabase (from libs.versions.toml)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)

    // Ktor (from libs.versions.toml)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
}