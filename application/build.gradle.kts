plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

import java.util.Properties

// Load and increment version from properties file
val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties()
if (versionPropsFile.exists()) {
    versionProps.load(versionPropsFile.inputStream())
}

val versionMajor = versionProps.getProperty("VERSION_MAJOR", "2").toInt()
val versionMinor = versionProps.getProperty("VERSION_MINOR", "0").toInt()
val versionPatch = versionProps.getProperty("VERSION_PATCH", "0").toInt()
val versionCodeValue = versionProps.getProperty("VERSION_CODE", "1").toInt()

// Increment patch version and save
tasks.register("incrementVersion") {
    doLast {
        val newPatch = versionPatch + 1
        val newVersionCode = versionCodeValue + 1
        versionProps.setProperty("VERSION_PATCH", newPatch.toString())
        versionProps.setProperty("VERSION_CODE", newVersionCode.toString())
        versionProps.store(versionPropsFile.outputStream(), null)
        println("Version incremented to $versionMajor.$versionMinor.$newPatch (code: $newVersionCode)")
    }
}

// Auto-increment on assembleDebug and assembleRelease
afterEvaluate {
    tasks.matching { it.name == "assembleDebug" || it.name == "assembleRelease" }.configureEach {
        dependsOn("incrementVersion")
    }
}

android {
    namespace = "com.samsara.polymath"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.samsara.polymath"
        minSdk = 24
        targetSdk = 34
        versionCode = versionCodeValue
        versionName = "$versionMajor.$versionMinor.$versionPatch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
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
        viewBinding = true
    }

    // Custom APK naming: samsara-debug/release.Major.minor.patch.apk
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val versionName = "$versionMajor.$versionMinor.$versionPatch"
            output.outputFileName = "samsara-${variant.buildType.name}-${versionName}.apk"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
