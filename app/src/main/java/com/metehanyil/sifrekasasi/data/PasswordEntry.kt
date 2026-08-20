package com.metehanyil.sifrekasasi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One saved credential.
 *
 * @param siteName short, human-friendly name (e.g. "Google")
 * @param siteDomain the site's domain/extension (e.g. "google.com")
 * @param username the username or e-mail used on that site
 * @param encryptedPassword Base64(iv || ciphertext) - see CryptoManager
 */
@Entity(tableName = "password_entries")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteName: String,
    val siteDomain: String,
    val username: String,
    val encryptedPassword: String
)
