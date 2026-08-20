package com.metehanyil.sifrekasasi.ui

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.metehanyil.sifrekasasi.R
import com.metehanyil.sifrekasasi.crypto.CryptoManager
import com.metehanyil.sifrekasasi.data.AppDatabase
import com.metehanyil.sifrekasasi.data.PasswordEntry
import com.metehanyil.sifrekasasi.databinding.ActivityAddEditBinding
import kotlinx.coroutines.launch

class AddEditActivity : BaseActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private var editingEntry: PasswordEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val entryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)
        if (entryId != -1L) {
            supportActionBar?.title = getString(R.string.edit_entry_title)
            lifecycleScope.launch {
                val entry = AppDatabase.getInstance(this@AddEditActivity).passwordDao().getById(entryId)
                entry?.let { populateFields(it) }
            }
        } else {
            supportActionBar?.title = getString(R.string.add_entry_title)
        }

        binding.btnSave.setOnClickListener { saveEntry() }
    }

    private fun populateFields(entry: PasswordEntry) {
        editingEntry = entry
        binding.etSiteName.setText(entry.siteName)
        binding.etSiteDomain.setText(entry.siteDomain)
        binding.etUsername.setText(entry.username)
        binding.etPassword.setText(runCatching { CryptoManager.decrypt(entry.encryptedPassword) }.getOrDefault(""))
    }

    private fun saveEntry() {
        val siteName = binding.etSiteName.text?.toString()?.trim().orEmpty()
        val siteDomain = binding.etSiteDomain.text?.toString()?.trim().orEmpty()
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        if (siteName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_required_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val encryptedPassword = CryptoManager.encrypt(password)
        val dao = AppDatabase.getInstance(this).passwordDao()

        lifecycleScope.launch {
            val current = editingEntry
            if (current != null) {
                dao.update(
                    current.copy(
                        siteName = siteName,
                        siteDomain = siteDomain,
                        username = username,
                        encryptedPassword = encryptedPassword
                    )
                )
            } else {
                dao.insert(
                    PasswordEntry(
                        siteName = siteName,
                        siteDomain = siteDomain,
                        username = username,
                        encryptedPassword = encryptedPassword
                    )
                )
            }
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }
}
