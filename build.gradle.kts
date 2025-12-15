// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // AÑADE ESTA LÍNEA:
    alias(libs.plugins.google.services) apply false
}