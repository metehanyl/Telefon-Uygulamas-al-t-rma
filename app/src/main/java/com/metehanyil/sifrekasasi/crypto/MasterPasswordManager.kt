package com.metehanyil.sifrekasasi.crypto

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Gates access to the app behind a master password. Only a salted PBKDF2
 * hash of the master password is ever stored (inside EncryptedSharedPreferences),
 * never the password itself. This master password does not derive the
 * encryption key used for the saved site passwords (see [CryptoManager]) -
 * it purely acts as a lock screen.
 */
object MasterPasswordManager {

    private const val PREFS_NAME = "sifre_kasasi_secure_prefs"
    private const val KEY_SALT = "master_pw_salt"
    private const val KEY_HASH = "master_pw_hash"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    private fun prefs(context: Context) = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isMasterPasswordSet(context: Context): Boolean =
        prefs(context).contains(KEY_HASH)

    fun setMasterPassword(context: Context, password: String) {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = hash(password, salt)
        prefs(context).edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
    }

    fun verifyMasterPassword(context: Context, password: String): Boolean {
        val p = prefs(context)
        val saltStr = p.getString(KEY_SALT, null) ?: return false
        val hashStr = p.getString(KEY_HASH, null) ?: return false
        val salt = Base64.decode(saltStr, Base64.NO_WRAP)
        val expected = Base64.decode(hashStr, Base64.NO_WRAP)
        return hash(password, salt).contentEquals(expected)
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
