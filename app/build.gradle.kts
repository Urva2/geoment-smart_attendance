plugins {
    alias(libs.plugins.androidApplication)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.attendance"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.attendance"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.support.annotations)
    implementation(libs.firebase.firestore)
    implementation(libs.biometric)
    implementation(libs.recyclerview)
    implementation(libs.datastore.core.android)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation("com.google.firebase:firebase-firestore:24.7.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.itextpdf:itextpdf:5.5.13.2")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.3") // Required for network requests


}
