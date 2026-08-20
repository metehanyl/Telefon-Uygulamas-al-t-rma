package com.metehanyil.sifrekasasi.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.metehanyil.sifrekasasi.AppLockState
import com.metehanyil.sifrekasasi.R
import com.metehanyil.sifrekasasi.crypto.MasterPasswordManager
import com.metehanyil.sifrekasasi.databinding.ActivityLockBinding

/**
 * Launcher activity. Doubles as "create master password" (first run) and
 * "enter master password" (every run after that).
 */
class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding
    private var isCreateMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isCreateMode = !MasterPasswordManager.isMasterPasswordSet(this)

        if (isCreateMode) {
            binding.tvTitle.text = getString(R.string.lock_create_title)
            binding.tvSubtitle.text = getString(R.string.lock_create_subtitle)
            binding.tilConfirmPassword.visibility = View.VISIBLE
            binding.btnSubmit.text = getString(R.string.lock_create_button)
        } else {
            binding.tvTitle.text = getString(R.string.lock_enter_title)
            binding.tvSubtitle.text = getString(R.string.lock_enter_subtitle)
            binding.tilConfirmPassword.visibility = View.GONE
            binding.btnSubmit.text = getString(R.string.lock_enter_button)
        }

        binding.btnSubmit.setOnClickListener { handleSubmit() }
    }

    private fun handleSubmit() {
        val password = binding.etPassword.text?.toString().orEmpty()
        binding.tvError.visibility = View.GONE

        if (isCreateMode) {
            val confirm = binding.etConfirmPassword.text?.toString().orEmpty()
            when {
                password.length < 4 -> showError(getString(R.string.error_password_too_short))
                password != confirm -> showError(getString(R.string.error_passwords_not_match))
                else -> {
                    MasterPasswordManager.setMasterPassword(this, password)
                    unlockAndProceed()
                }
            }
        } else {
            if (MasterPasswordManager.verifyMasterPassword(this, password)) {
                unlockAndProceed()
            } else {
                showError(getString(R.string.error_wrong_password))
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun unlockAndProceed() {
        AppLockState.isUnlocked = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
