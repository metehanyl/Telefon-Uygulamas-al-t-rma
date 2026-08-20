package com.metehanyil.sifrekasasi.data

import android.content.Context
import android.net.Uri
import com.metehanyil.sifrekasasi.crypto.CryptoManager
import com.metehanyil.sifrekasasi.util.CsvParser
import java.net.URI

data class ImportResult(
    val imported: Int,
    val skippedDuplicate: Int,
    val skippedInvalid: Int
)

/**
 * Imports a CSV file exported from Chrome's password manager
 * (chrome://password-manager/settings -> "Export passwords"). The expected
 * header contains at least "url", "username" and "password" columns (Chrome
 * also includes "name" and sometimes "note" - column order/extra columns
 * don't matter, they're located by name).
 */
object PasswordImporter {

    suspend fun importFromCsv(context: Context, uri: Uri, dao: PasswordDao): ImportResult {
        val text = context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).readText()
        } ?: return ImportResult(0, 0, 0)

        val rows = CsvParser.parse(text)
        if (rows.isEmpty()) return ImportResult(0, 0, 0)

        val header = rows.first().map { it.trim().lowercase() }
        val nameIdx = header.indexOf("name")
        val urlIdx = header.indexOf("url")
        val usernameIdx = header.indexOf("username")
        val passwordIdx = header.indexOf("password")

        if (urlIdx == -1 || usernameIdx == -1 || passwordIdx == -1) {
            return ImportResult(0, 0, (rows.size - 1).coerceAtLeast(0))
        }

        val existingKeys = dao.getAllOnce()
            .map { it.siteDomain.lowercase() to it.username.lowercase() }
            .toMutableSet()

        var imported = 0
        var skippedDuplicate = 0
        var skippedInvalid = 0

        for (r in rows.drop(1)) {
            val url = r.getOrNull(urlIdx)?.trim().orEmpty()
            val username = r.getOrNull(usernameIdx)?.trim().orEmpty()
            val password = r.getOrNull(passwordIdx)?.trim().orEmpty()
            val rawName = nameIdx.takeIf { it != -1 }?.let { r.getOrNull(it)?.trim() }.orEmpty()

            if (url.isEmpty() || password.isEmpty()) {
                skippedInvalid++
                continue
            }

            val host = extractHost(url)
            val siteDomain = host ?: url
            val siteName = rawName.ifBlank { host ?: url }

            val key = siteDomain.lowercase() to username.lowercase()
            if (!existingKeys.add(key)) {
                skippedDuplicate++
                continue
            }

            dao.insert(
                PasswordEntry(
                    siteName = siteName,
                    siteDomain = siteDomain,
                    username = username,
                    encryptedPassword = CryptoManager.encrypt(password)
                )
            )
            imported++
        }

        return ImportResult(imported, skippedDuplicate, skippedInvalid)
    }

    private fun extractHost(url: String): String? {
        return try {
            val withScheme = if (url.contains("://")) url else "https://$url"
            URI(withScheme).host?.removePrefix("www.")
        } catch (e: Exception) {
            null
        }
    }
}
