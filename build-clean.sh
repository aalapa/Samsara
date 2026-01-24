#!/bin/bash
# Clean build script that bypasses Android Studio's signing injection

export ANDROID_SDK_ROOT=/Users/ragnor/Library/Android/sdk
export JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr/Contents/Home

# Clear all signing properties
unset ANDROID_INJECTED_SIGNING_STORE_FILE
unset ANDROID_INJECTED_SIGNING_STORE_PASSWORD
unset ANDROID_INJECTED_SIGNING_KEY_ALIAS
unset ANDROID_INJECTED_SIGNING_KEY_PASSWORD

# Navigate to project
cd /Users/ragnor/StudioProjects/Samsara

# Clean
rm -rf app/build .gradle

echo "=== Building without Android Studio injection ==="
echo "Using Java: $JAVA_HOME"
echo ""

# Use the Android SDK's Gradle instead if available
if [ -f "$ANDROID_SDK_ROOT/tools/templates/gradle/wrapper/gradlew" ]; then
    echo "Using Android SDK gradle wrapper"
    $ANDROID_SDK_ROOT/tools/templates/gradle/wrapper/gradlew assembleDebug --no-daemon --stacktrace
else
    echo "ERROR: Could not find Gradle. Please install Gradle or use Android Studio's terminal."
    echo ""
    echo "Try running in Android Studio's Terminal:"
    echo "  cd /Users/ragnor/StudioProjects/Samsara"
    echo "  gradle assembleDebug"
fi


