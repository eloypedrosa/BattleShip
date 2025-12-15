plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 1. AÑADIDO: Necesario para que Firebase funcione
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.eloypedrosa.battleship"
    compileSdk = 36

    // 2. CORREGIDO: Eliminada la frase de texto que causaba error de sintaxis

    defaultConfig {
        applicationId = "com.eloypedrosa.battleship"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
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
}

dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(platform("com.google.firebase:firebase-bom:33.6.0")) // Use the latest version

    // Declare the dependency for the Cloud Firestore library
    // REMOVE "-ktx" from the end
    implementation("com.google.firebase:firebase-firestore:26.0.2")
    implementation("com.google.firebase:firebase-auth:24.0.1")


    // Coroutines & Lifecycle
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // UI & Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // 3. CORREGIDO: Eliminado 'libs.material.v190' duplicado. Dejamos solo la versión nueva.
    implementation(libs.material)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}