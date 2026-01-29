package com.samsara.polymath

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.samsara.polymath.databinding.ActivityPinSetupBinding
import com.samsara.polymath.util.AuthManager

class PinSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinSetupBinding
    private lateinit var authManager: AuthManager
    private val pinDigits = mutableListOf<String>()
    private val pinDots = mutableListOf<View>()
    
    private var firstPin: String? = null
    private var isConfirmationStage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
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
        updatePinDots()
        
        binding.skipButton.setOnClickListener {
            goToMainActivity()
        }
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

    private fun addDigit(digit: String) {
        if (pinDigits.size < 4) {
            pinDigits.add(digit)
            updatePinDots()
            
            if (pinDigits.size == 4) {
                handlePinComplete()
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

    private fun handlePinComplete() {
        val enteredPin = pinDigits.joinToString("")
        
        if (!isConfirmationStage) {
            // First entry
            firstPin = enteredPin
            isConfirmationStage = true
            binding.titleTextView.text = getString(R.string.confirm_pin)
            binding.instructionTextView.text = getString(R.string.pin_confirm_instruction)
            clearPin()
        } else {
            // Confirmation entry
            if (enteredPin == firstPin) {
                // PINs match, save it
                authManager.setPin(enteredPin)
                showBiometricOption()
            } else {
                // PINs don't match
                showError(getString(R.string.pins_dont_match))
                // Reset to first entry
                isConfirmationStage = false
                firstPin = null
                binding.titleTextView.text = getString(R.string.setup_pin)
                binding.instructionTextView.text = getString(R.string.pin_setup_instruction)
                clearPin()
            }
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

    private fun showBiometricOption() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.enable_fingerprint))
            .setMessage(getString(R.string.enable_fingerprint_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                authManager.setBiometricEnabled(true)
                goToMainActivity()
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                authManager.setBiometricEnabled(false)
                goToMainActivity()
            }
            .setCancelable(false)
            .show()
    }

    private fun goToMainActivity() {
        authManager.setAuthenticated(true)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Allow back to skip setup
        goToMainActivity()
    }
}


