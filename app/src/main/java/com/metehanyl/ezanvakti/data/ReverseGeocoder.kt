package com.metehanyl.ezanvakti.data

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class GeoArea(val il: String?, val ilce: String?)

/**
 * Enlem/boylamı il ve ilçe adına çevirir. Cihazda geocoder servisi yoksa (ör. bazı Play
 * Services'siz cihazlar) ya da internet yoksa null döner; bu durumda kullanıcı konumu
 * manuel olarak seçebilir.
 */
suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): GeoArea =
    withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext GeoArea(null, null)
            val geocoder = Geocoder(context, Locale("tr", "TR"))
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocation(latitude, longitude, 1)
            val address = results?.firstOrNull() ?: return@withContext GeoArea(null, null)
            GeoArea(il = address.adminArea, ilce = address.subAdminArea ?: address.locality)
        } catch (e: Exception) {
            GeoArea(null, null)
        }
    }
