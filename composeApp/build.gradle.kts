import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.material)
            // UI / Platform File Selector
            implementation(libs.filekit.core)
            // DI
//            implementation(libs.kotlin.inject.runtime)
//            implementation(libs.kotlin.inject.anvil.runtime)
//            implementation(libs.kotlin.inject.anvil.runtime.optional)
            // Android Platform background task scheduling
            implementation(libs.work.manager)
            // Database
            implementation(libs.room.runtime)
        }
        commonMain.dependencies {
            // UI
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // UI / Platform File Selector
            implementation(libs.filekit.compose)
            // UI Navigation
            implementation(libs.navigation.compose)
            // ViewModel
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            // DI
            implementation(libs.kotlin.inject.runtime)
            implementation(libs.kotlin.inject.anvil.runtime)
            implementation(libs.kotlin.inject.anvil.runtime.optional)
            // User Settings Store
            implementation(libs.datastore.preferences)
            // Database
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            // Parsing
            implementation(libs.kotlinx.serialization.json)
            // Network
            implementation(libs.ktor.network)
            // Fonts
            implementation(libs.ui.google.fonts)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.room.runtime)
        }
    }

    configureCommonMainKsp()
}

android {
    namespace = "com.github.jack_davis_gh.ns_usbloader"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.github.jack_davis_gh.ns_usbloader"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)


//    add("kspCommonMainMetadata", libs.kotlin.inject.compiler)
    add("kspCommonMainMetadata", libs.kotlin.inject.anvil.compiler)

    listOf("kspAndroid", "kspDesktop")
        .forEach {
            add(it, libs.kotlin.inject.compiler)
            add(it, libs.kotlin.inject.anvil.compiler)
            add(it, libs.room.compiler)
        }
}

compose.desktop {
    application {
        mainClass = "com.github.jack_davis_gh.ns_usbloader.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.github.jack_davis_gh.ns_usbloader"
            packageVersion = "1.0.0"
        }
    }
}

fun KotlinMultiplatformExtension.configureCommonMainKsp() {
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }

    project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
        if(name != "kspCommonMainKotlinMetadata") {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}