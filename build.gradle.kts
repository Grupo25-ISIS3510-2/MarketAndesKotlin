// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.0")  // Usa la versión correcta de Gradle
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10") // Usa la versión correcta de Kotlin
        classpath("com.google.gms:google-services:4.4.2")  // Asegúrate de que la versión esté actualizada
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}