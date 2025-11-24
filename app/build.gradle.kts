plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.heodongun.ugoal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.heodongun.ugoal"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/native-image/**"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)


    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.bundles.lifecycle)

    // Coroutines
    implementation(libs.bundles.coroutines)


    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)


    // Room for offline-first architecture
    implementation(libs.bundles.room)

    // DataStore for settings
    implementation(libs.androidx.datastore.preferences)

    // Work Manager for background sync
    implementation(libs.androidx.work.runtime.ktx)

    // Charts for statistics
    implementation(libs.mpandroidchart)

    // Lottie for animations
    implementation(libs.lottie.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}