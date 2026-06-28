package com.metehanyl.ezanvakti.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Read-only cross-app contract exposing the most recently cached prayer times,
 * so other apps on the device (e.g. Çalar Saat) can read today's İmsak time
 * without re-implementing the Diyanet fetch/cache logic.
 *
 * Single row, column order: tarih, imsak, fetched_at_millis, is_today, sehir_adi, ilce_adi.
 * Returns an empty cursor if nothing has been cached yet (app never opened/synced).
 */
class PrayerProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(COLUMNS)
        val bundle = context?.let { PrayerCache(it).load() }
        if (bundle != null) {
            val (day, isToday) = bundle.todayOrClosest()
            cursor.addRow(
                arrayOf(
                    day.tarih,
                    day.imsak,
                    bundle.fetchedAtEpochMillis,
                    if (isToday) 1 else 0,
                    bundle.sehirAdi,
                    bundle.ilceAdi
                )
            )
        }
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.$AUTHORITY.imsak"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    companion object {
        const val AUTHORITY = "com.metehanyl.ezanvakti.provider"
        val IMSAK_URI: Uri = Uri.parse("content://$AUTHORITY/imsak")

        val COLUMNS = arrayOf("tarih", "imsak", "fetched_at_millis", "is_today", "sehir_adi", "ilce_adi")
    }
}
