plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.blogspot.developersu.ns_usbloader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blogspot.developersu.ns_usbloader"
        minSdk = 29
        targetSdk = 34
        versionCode = 7
        versionName = "5.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        android.defaultConfig.vectorDrawables.useSupportLibrary = true
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
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
//    implementation(fileTree(dir: "libs", include: ["*.jar"]))
    implementation(platform(libs.compose.bom))
    implementation(libs.material3)
    implementation(libs.ui.tooling.preview)
    implementation(libs.ui.google.fonts)
    debugImplementation(libs.ui.tooling)
    implementation(libs.activity.compose)
    // Optional - Integration with ViewModels
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.datastore.preferences)

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.fragment)

    implementation(libs.accompanist.permissions)

    implementation(libs.appcompat)
//    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
//    testImplementation("junit:junit:4.13")
//    androidTestImplementation("androidx.test:runner:1.3.0")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
//    implementation(libs.recyclerview)
//    implementation(libs.cardview)
//    implementation(libs.material)
    implementation(libs.work.manager)

    implementation(libs.hilt.navigation.compose)

    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    ksp(libs.hilt.android.compiler)

    implementation(libs.kotlinx.serialization.json)

    // Enables FileKit without Compose dependencies
    implementation(libs.filekit.core)
    // Enables FileKit with Composable utilities
    implementation(libs.filekit.compose)
}
