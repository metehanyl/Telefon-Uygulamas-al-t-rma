package com.metehanyil.sifrekasasi.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.metehanyil.sifrekasasi.AppLockState
import com.metehanyil.sifrekasasi.AppScope
import com.metehanyil.sifrekasasi.R
import com.metehanyil.sifrekasasi.crypto.CryptoManager
import com.metehanyil.sifrekasasi.data.AppDatabase
import com.metehanyil.sifrekasasi.data.PasswordEntry
import com.metehanyil.sifrekasasi.data.PasswordImporter
import com.metehanyil.sifrekasasi.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PasswordAdapter
    private var allEntries: List<PasswordEntry> = emptyList()

    private val importCsvLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { runImport(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = PasswordAdapter(
            onItemClick = { entry ->
                val intent = Intent(this, AddEditActivity::class.java)
                intent.putExtra(AddEditActivity.EXTRA_ENTRY_ID, entry.id)
                startActivity(intent)
            },
            onCopyClick = { entry -> copyPasswordToClipboard(entry) },
            onDeleteClick = { entry -> confirmDelete(entry) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }

        val dao = AppDatabase.getInstance(this).passwordDao()
        lifecycleScope.launch {
            dao.getAll().collect { entries ->
                allEntries = entries
                applyFilter(binding.searchView.query?.toString().orEmpty())
                binding.tvEmpty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilter(newText.orEmpty())
                return true
            }
        })
    }

    private fun applyFilter(query: String) {
        val filtered = if (query.isBlank()) {
            allEntries
        } else {
            allEntries.filter {
                it.siteName.contains(query, ignoreCase = true) ||
                    it.siteDomain.contains(query, ignoreCase = true) ||
                    it.username.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
    }

    private fun copyPasswordToClipboard(entry: PasswordEntry) {
        val password = runCatching { CryptoManager.decrypt(entry.encryptedPassword) }.getOrNull() ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), password))
        Toast.makeText(this, getString(R.string.password_copied), Toast.LENGTH_SHORT).show()
    }

    private fun confirmDelete(entry: PasswordEntry) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_confirm_title))
            .setMessage(getString(R.string.delete_confirm_message, entry.siteName))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launch {
                    AppDatabase.getInstance(this@MainActivity).passwordDao().delete(entry)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_lock -> {
                AppLockState.isUnlocked = false
                startActivity(Intent(this, LockActivity::class.java))
                finish()
                return true
            }
            R.id.action_import -> {
                showImportIntroDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showImportIntroDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.import_intro_title))
            .setMessage(getString(R.string.import_intro_message))
            .setPositiveButton(getString(R.string.import_pick_file)) { _, _ ->
                importCsvLauncher.launch(arrayOf("*/*"))
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun runImport(uri: Uri) {
        val progressDialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.importing))
            .setCancelable(false)
            .show()

        // Runs on an app-lifetime scope (not lifecycleScope): the system file
        // picker briefly backgrounds this activity, and we don't want a
        // recreation/finish in the meantime to cut the import off partway
        // through. UI updates below check isFinishing/isDestroyed before
        // touching this activity's views.
        val dao = AppDatabase.getInstance(applicationContext).passwordDao()
        val appContext = applicationContext

        AppScope.scope.launch {
            val result = runCatching { PasswordImporter.importFromCsv(appContext, uri, dao) }

            withContext(Dispatchers.Main) {
                runCatching { progressDialog.dismiss() }
                if (isFinishing || isDestroyed) return@withContext

                result.onSuccess { r ->
                    if (r.imported == 0 && r.skippedDuplicate == 0 && r.skippedInvalid > 0) {
                        showSimpleDialog(getString(R.string.import_result_title), getString(R.string.import_error))
                    } else {
                        showSimpleDialog(
                            getString(R.string.import_result_title),
                            getString(R.string.import_result_message, r.imported, r.skippedDuplicate, r.skippedInvalid)
                        )
                    }
                }.onFailure {
                    showSimpleDialog(getString(R.string.import_result_title), getString(R.string.import_error))
                }
            }
        }
    }

    private fun showSimpleDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
}
