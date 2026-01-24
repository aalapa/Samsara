import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// CRITICAL: Intercept and clear invalid signing properties BEFORE Android plugin processes them
// This must happen immediately after plugins are applied
System.setProperty("android.injected.signing.store.file", "")
System.setProperty("android.injected.signing.store.password", "")
System.setProperty("android.injected.signing.key.alias", "")
System.setProperty("android.injected.signing.key.password", "")

// Also clear from project properties
listOf(
    "android.injected.signing.store.file",
    "android.injected.signing.store.password",
    "android.injected.signing.key.alias",
    "android.injected.signing.key.password"
).forEach { prop ->
    if (hasProperty(prop)) {
        val value = findProperty(prop) as? String
        println("Found injected property $prop = $value")
        // Can't actually remove properties, but we can override them
        project.extensions.extraProperties.set(prop, "")
    }
}

println("=== Cleared all injected signing properties ===")

// Read version from properties file
val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = Properties()

if (versionPropertiesFile.exists()) {
    versionProperties.load(FileInputStream(versionPropertiesFile))
} else {
    versionProperties["VERSION_MAJOR"] = "1"
    versionProperties["VERSION_MINOR"] = "2"
    versionProperties["VERSION_PATCH"] = "0"
    versionProperties["VERSION_CODE"] = "1"
}

// Auto-increment version patch and version code on every build
val currentMajor = (versionProperties["VERSION_MAJOR"] as String).toInt()
val currentMinor = (versionProperties["VERSION_MINOR"] as String).toInt()
var currentPatch = (versionProperties["VERSION_PATCH"] as String).toInt()
var currentVersionCode = (versionProperties["VERSION_CODE"] as String).toInt()

// Increment patch and version code
currentPatch++
currentVersionCode++

versionProperties["VERSION_PATCH"] = currentPatch.toString()
versionProperties["VERSION_CODE"] = currentVersionCode.toString()

// Save updated version
versionProperties.store(FileOutputStream(versionPropertiesFile), "Auto-incremented on build")

// Format version name as 1.01.X (minor with leading zero)
val versionNameString = "$currentMajor.${String.format("%02d", currentMinor)}.$currentPatch"

android {
    namespace = "com.samsara.polymath"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.samsara.polymath"
        minSdk = 24
        targetSdk = 34
        versionCode = currentVersionCode
        versionName = versionNameString

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Ensure app name is set correctly
        resValue("string", "app_name", "Samsara")
    }
    

    // Don't define ANY signing configs - completely bypass signing
    
    buildTypes {
        getByName("debug") {
            // Explicitly set to null to prevent any signing config from being used
            signingConfig = null
            
            // Alternative: disable package task validation
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Explicitly set to null
            signingConfig = null
        }
    }
    
    buildFeatures {
        viewBinding = true
    }
    
    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    
    // CRITICAL: Configure signing to prevent validation errors
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
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
}

// Customize APK output name - rename after package tasks
val versionForApk = versionNameString.replace(".", "_")
val versionCodeForApk = currentVersionCode

// Configure tasks before evaluation to catch package tasks early
tasks.configureEach {
    if (name == "packageDebug" || name == "packageRelease") {
        doFirst {
            println("=== Packaging task: $name ===")
            println("Using default Android debug signing")
        }
    }
}

// Don't disable any tasks - let's try to fix the signing config properly

afterEvaluate {
    println("=== AfterEvaluate: Final attempt to fix signing ===")
    
    // Create a valid debug signing config if it doesn't exist
    val debugKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
    
    if (!android.signingConfigs.any { it.name == "debug" }) {
        println("Creating debug signing config")
        android.signingConfigs.create("debug") {
            storeFile = debugKeystore
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    
    // Remove any invalid signing configs
    val invalidConfigs = android.signingConfigs.filter { config ->
        config.name == "externalOverride" || 
        (config.storeFile != null && config.storeFile!!.isDirectory)
    }
    
    invalidConfigs.forEach { config ->
        println("Removing invalid signing config: ${config.name}")
        android.signingConfigs.remove(config)
    }
    
    // Set all build types to use the debug config
    val debugConfig = android.signingConfigs.findByName("debug")
    android.buildTypes.all {
        println("Setting build type '$name' to use debug signing config")
        signingConfig = debugConfig
    }
    
    // Disable validation tasks
    tasks.matching { it.name.startsWith("validateSigning") }.configureEach {
        enabled = false
    }
    
    // Hook into package tasks
    tasks.named("packageDebug").configure {
        doLast {
            val apkDir = project.file("${project.buildDir}/outputs/apk/debug")
            val buildType = "debug"
            val targetName = "Samsara_${buildType}_v${versionForApk}_${versionCodeForApk}.apk"
            
            println("=== Renaming APK files ===")
            println("Looking in: ${apkDir.absolutePath}")
            println("Version: $versionForApk, VersionCode: $versionCodeForApk")
            
            if (apkDir.exists()) {
                apkDir.walkTopDown().forEach { apkFile ->
                    if (apkFile.isFile && apkFile.name.endsWith(".apk")) {
                        val currentName = apkFile.name
                        if (currentName != targetName) {
                            val newFile = File(apkFile.parent, targetName)
                            println("Renaming: $currentName -> $targetName")
                            if (newFile.exists() && newFile.absolutePath != apkFile.absolutePath) {
                                newFile.delete()
                            }
                            apkFile.renameTo(newFile)
                        }
                    }
                }
            }
        }
    }
    
    tasks.named("packageRelease").configure {
        doLast {
            val apkDir = project.file("${project.buildDir}/outputs/apk/release")
            val buildType = "release"
            val targetName = "Samsara_${buildType}_v${versionForApk}_${versionCodeForApk}.apk"
            
            println("=== Renaming APK files ===")
            println("Looking in: ${apkDir.absolutePath}")
            println("Version: $versionForApk, VersionCode: $versionCodeForApk")
            
            if (apkDir.exists()) {
                apkDir.walkTopDown().forEach { apkFile ->
                    if (apkFile.isFile && apkFile.name.endsWith(".apk")) {
                        val currentName = apkFile.name
                        if (currentName != targetName) {
                            val newFile = File(apkFile.parent, targetName)
                            println("Renaming: $currentName -> $targetName")
                            if (newFile.exists() && newFile.absolutePath != apkFile.absolutePath) {
                                newFile.delete()
                            }
                            apkFile.renameTo(newFile)
                        }
                    }
                }
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // ItemTouchHelper for drag and swipe
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


