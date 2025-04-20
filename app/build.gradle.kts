plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.relay") version "0.3.12"
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"



}

android {
    namespace = "com.uniandes.marketandes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.uniandes.marketandes"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.ktx.v160)
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.gms:play-services-maps:17.0.0")
    implementation("com.google.maps.android:maps-compose:2.2.0")
    implementation("com.google.android.gms:play-services-location:17.0.0")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.json:json:20210307")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx")


    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    //timber
    implementation("com.jakewharton.timber:timber:4.7.1")
    //lottie
    implementation("com.airbnb.android:lottie:4.2.0")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.2.1")
    implementation ("androidx.navigation:navigation-compose:2.5.3")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.firebase.auth.ktx)

    //Datos biometricos
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")

    // Room components
    implementation("androidx.room:room-runtime:2.7.0-alpha01")
    implementation("androidx.room:room-ktx:2.7.0-alpha01")
    ksp("androidx.room:room-compiler:2.7.0-alpha01")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    implementation(libs.androidx.credentials)
    implementation(libs.play.services.analytics.impl)
    implementation(libs.androidx.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}