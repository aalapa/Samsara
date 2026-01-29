package com.samsara.polymath

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.samsara.polymath.databinding.ActivitySettingsBinding
import com.samsara.polymath.util.AuthManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val isAuthEnabled = authManager.isAuthEnabled()
        
        // PIN Lock Switch
        binding.pinLockSwitch.isChecked = isAuthEnabled
        
        if (isAuthEnabled) {
            binding.pinLockTitleTextView.text = getString(R.string.disable_pin)
            binding.changePinCard.visibility = View.VISIBLE
            
            // Check biometric availability
            val biometricManager = BiometricManager.from(this)
            val canUseBiometric = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            ) == BiometricManager.BIOMETRIC_SUCCESS
            
            if (canUseBiometric) {
                binding.biometricCard.visibility = View.VISIBLE
                binding.biometricSwitch.isChecked = authManager.isBiometricEnabled()
            } else {
                binding.biometricCard.visibility = View.GONE
            }
        } else {
            binding.pinLockTitleTextView.text = getString(R.string.enable_pin)
            binding.changePinCard.visibility = View.GONE
            binding.biometricCard.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        // PIN Lock Switch
        binding.pinLockSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable PIN - go to PIN setup
                val intent = Intent(this, PinSetupActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Disable PIN - verify current PIN first
                if (authManager.isAuthEnabled()) {
                    showDisablePinConfirmation()
                }
            }
        }

        // Change PIN
        binding.changePinCard.setOnClickListener {
            showChangePinDialog()
        }

        // Biometric Switch
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            authManager.setBiometricEnabled(isChecked)
            if (isChecked) {
                Toast.makeText(this, getString(R.string.biometric_enabled), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.biometric_disabled), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDisablePinConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Disable PIN Lock")
            .setMessage("Are you sure you want to disable PIN lock? Your app data will no longer be protected.")
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                authManager.disableAuth()
                Toast.makeText(this, getString(R.string.pin_disabled), Toast.LENGTH_SHORT).show()
                setupUI()
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                binding.pinLockSwitch.isChecked = true
            }
            .setOnCancelListener {
                binding.pinLockSwitch.isChecked = true
            }
            .show()
    }

    private fun showChangePinDialog() {
        val intent = Intent(this, PinSetupActivity::class.java)
        intent.putExtra("isChangingPin", true)
        startActivity(intent)
        finish()
    }
}


