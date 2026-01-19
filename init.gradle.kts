// Gradle initialization script to prevent Android Studio signing config injection
// This runs before any project configuration

// Clear system properties immediately
println("=== INIT SCRIPT: Clearing signing properties ===")
System.clearProperty("android.injected.signing.store.file")
System.clearProperty("android.injected.signing.store.password")
System.clearProperty("android.injected.signing.key.alias")
System.clearProperty("android.injected.signing.key.password")

gradle.beforeProject {
    // Clear project properties before each project is configured
    println("INIT: Clearing injected properties for project: $name")
    
    // Override all possible injected property sources
    listOf(
        "android.injected.signing.store.file",
        "android.injected.signing.store.password",
        "android.injected.signing.key.alias",
        "android.injected.signing.key.password"
    ).forEach { prop ->
        if (hasProperty(prop)) {
            val value = findProperty(prop)
            println("INIT: Found $prop = $value, clearing...")
        }
        // Force override to empty string
        extensions.extraProperties.set(prop, "")
    }
}


