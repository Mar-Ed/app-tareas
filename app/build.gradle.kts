plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.organizadordetareas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.organizadordetareas"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)

    // Fragment KTX (lifecycle-aware Fragment helpers)
    implementation(libs.androidx.fragment.ktx)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Material Design Components (1.13.0 — Material Design 3)
    implementation(libs.material)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // Google Maps
    implementation(libs.play.services.maps)

    // Retrofit (HTTP client para REST API)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}