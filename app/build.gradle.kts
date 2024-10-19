plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
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
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("demo") {
            // Assigns this product flavor to the "version" flavor dimension.
            // If you are using only one dimension, this property is optional,
            // and the plugin automatically assigns all the module's flavors to
            // that dimension.
            resourceConfigurations += listOf("en")
            dimension = "version"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
        }
        create("full") {
            dimension = "version"
            applicationIdSuffix = ".full"
            versionNameSuffix = "-full"
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
    implementation(libs.activity.compose)

    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)

    debugImplementation(libs.ui.tooling)
    implementation(libs.ui.tooling.preview)

    implementation(libs.ui.google.fonts)

    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)

    implementation(libs.accompanist.permissions)

    implementation(libs.appcompat)
    implementation(libs.core.ktx)
//    testImplementation("junit:junit:4.13")
//    androidTestImplementation("androidx.test:runner:1.3.0")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    implementation(libs.work.manager)

    implementation(libs.hilt.navigation.fragment)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    ksp(libs.hilt.android.compiler)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.coroutines.android)

    // Enables FileKit without Compose dependencies
    implementation(libs.filekit.core)
    // Enables FileKit with Composable utilities
    implementation(libs.filekit.compose)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
}
