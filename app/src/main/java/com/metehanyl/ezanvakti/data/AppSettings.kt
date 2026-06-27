package com.metehanyl.ezanvakti.data

import android.content.Context
import com.metehanyl.ezanvakti.data.model.SelectedLocation

/** Kullanıcının manuel olarak seçtiği il/ilçe ve bildirim tercihini saklar. */
class AppSettings(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getManualLocation(): SelectedLocation? {
        val sehirId = prefs.getString(KEY_SEHIR_ID, null) ?: return null
        val sehirAdi = prefs.getString(KEY_SEHIR_ADI, null) ?: return null
        val ilceId = prefs.getString(KEY_ILCE_ID, null) ?: return null
        val ilceAdi = prefs.getString(KEY_ILCE_ADI, null) ?: return null
        return SelectedLocation(sehirId, sehirAdi, ilceId, ilceAdi)
    }

    fun setManualLocation(location: SelectedLocation?) {
        if (location == null) {
            prefs.edit()
                .remove(KEY_SEHIR_ID).remove(KEY_SEHIR_ADI)
                .remove(KEY_ILCE_ID).remove(KEY_ILCE_ADI)
                .apply()
        } else {
            prefs.edit()
                .putString(KEY_SEHIR_ID, location.sehirId)
                .putString(KEY_SEHIR_ADI, location.sehirAdi)
                .putString(KEY_ILCE_ID, location.ilceId)
                .putString(KEY_ILCE_ADI, location.ilceAdi)
                .apply()
        }
    }

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    /** Kullanıcı koyu temayı elle değiştirmediyse [systemDefault] (cihaz teması) kullanılır. */
    fun isDarkThemeEnabled(systemDefault: Boolean): Boolean =
        prefs.getBoolean(KEY_DARK_THEME, systemDefault)

    fun setDarkThemeEnabled(value: Boolean) =
        prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    companion object {
        private const val PREFS_NAME = "ezan_vakti_settings"
        private const val KEY_SEHIR_ID = "manual_sehir_id"
        private const val KEY_SEHIR_ADI = "manual_sehir_adi"
        private const val KEY_ILCE_ID = "manual_ilce_id"
        private const val KEY_ILCE_ADI = "manual_ilce_adi"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_DARK_THEME = "dark_theme_enabled"
    }
}
