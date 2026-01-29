package com.samsara.polymath.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages authentication settings and PIN storage securely
 */
class AuthManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "samsara_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val regularPrefs: SharedPreferences = context.getSharedPreferences(
        "samsara_auth_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_PIN = "user_pin"
        private const val KEY_AUTH_ENABLED = "auth_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated_session"
    }
    
    /**
     * Check if authentication is set up
     */
    fun isAuthEnabled(): Boolean {
        return regularPrefs.getBoolean(KEY_AUTH_ENABLED, false)
    }
    
    /**
     * Check if biometric is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return regularPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    /**
     * Check if user is authenticated in current session
     */
    fun isAuthenticated(): Boolean {
        return regularPrefs.getBoolean(KEY_IS_AUTHENTICATED, false)
    }
    
    /**
     * Set PIN and enable authentication
     */
    fun setPin(pin: String) {
        encryptedPrefs.edit()
            .putString(KEY_PIN, pin)
            .apply()
        regularPrefs.edit()
            .putBoolean(KEY_AUTH_ENABLED, true)
            .apply()
    }
    
    /**
     * Verify PIN
     */
    fun verifyPin(pin: String): Boolean {
        val storedPin = encryptedPrefs.getString(KEY_PIN, null)
        return storedPin == pin
    }
    
    /**
     * Enable/disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        regularPrefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }
    
    /**
     * Mark user as authenticated in current session
     */
    fun setAuthenticated(authenticated: Boolean) {
        regularPrefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, authenticated)
            .apply()
    }
    
    /**
     * Disable authentication completely (requires PIN verification)
     */
    fun disableAuth() {
        encryptedPrefs.edit().remove(KEY_PIN).apply()
        regularPrefs.edit()
            .putBoolean(KEY_AUTH_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .putBoolean(KEY_IS_AUTHENTICATED, false)
            .apply()
    }
    
    /**
     * Clear authentication session (lock app)
     */
    fun lockApp() {
        setAuthenticated(false)
    }
}


