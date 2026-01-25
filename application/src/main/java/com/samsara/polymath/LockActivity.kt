package com.samsara.polymath

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.samsara.polymath.databinding.ActivityLockBinding
import com.samsara.polymath.util.AuthManager

class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding
    private lateinit var authManager: AuthManager
    private val pinDigits = mutableListOf<String>()
    private val pinDots = mutableListOf<View>()
    
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        // Initialize PIN dots list
        pinDots.addAll(listOf(
            binding.pinDot1,
            binding.pinDot2,
            binding.pinDot3,
            binding.pinDot4
        ))

        setupPinPad()
        setupBiometric()
        updatePinDots()
    }

    private fun setupPinPad() {
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6, binding.btn7,
            binding.btn8, binding.btn9
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                addDigit(button.text.toString())
            }
        }

        binding.btnBackspace.setOnClickListener {
            removeLastDigit()
        }
    }

    private fun setupBiometric() {
        val biometricManager = BiometricManager.from(this)
        
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric available
                if (authManager.isBiometricEnabled()) {
                    binding.biometricButton.visibility = View.VISIBLE
                    setupBiometricPrompt()
                    binding.biometricButton.setOnClickListener {
                        showBiometricPrompt()
                    }
                    // Auto-show biometric on launch
                    showBiometricPrompt()
                } else {
                    binding.biometricButton.visibility = View.GONE
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                binding.biometricButton.visibility = View.GONE
            }
        }
    }

    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && 
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(this@LockActivity, 
                        "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                unlockApp()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@LockActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Samsara")
            .setSubtitle("Use your fingerprint to unlock")
            .setNegativeButtonText("Use PIN")
            .build()
    }

    private fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }

    private fun addDigit(digit: String) {
        if (pinDigits.size < 4) {
            pinDigits.add(digit)
            updatePinDots()
            
            if (pinDigits.size == 4) {
                verifyPin()
            }
        }
    }

    private fun removeLastDigit() {
        if (pinDigits.isNotEmpty()) {
            pinDigits.removeAt(pinDigits.size - 1)
            updatePinDots()
            binding.errorTextView.visibility = View.INVISIBLE
        }
    }

    private fun updatePinDots() {
        pinDots.forEachIndexed { index, dot ->
            if (index < pinDigits.size) {
                dot.setBackgroundResource(R.drawable.pin_dot_filled)
            } else {
                dot.setBackgroundResource(R.drawable.pin_dot_empty)
            }
        }
    }

    private fun verifyPin() {
        val enteredPin = pinDigits.joinToString("")
        
        if (authManager.verifyPin(enteredPin)) {
            unlockApp()
        } else {
            showError("Incorrect PIN")
            clearPin()
        }
    }

    private fun showError(message: String) {
        binding.errorTextView.text = message
        binding.errorTextView.visibility = View.VISIBLE
    }

    private fun clearPin() {
        pinDigits.clear()
        updatePinDots()
    }

    private fun unlockApp() {
        authManager.setAuthenticated(true)
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back - user must authenticate
        // Do nothing
    }
}

