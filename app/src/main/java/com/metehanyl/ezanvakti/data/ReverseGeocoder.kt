package com.metehanyl.ezanvakti.data

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class GeoArea(val il: String?, val ilce: String?)

/**
 * Enlem/boylamı il ve ilçe adına çevirir. Önce cihazın sistem Geocoder'ı denenir; bu servis
 * Google Play Services olmayan ya da geocoding arka ucu çalışmayan cihazlarda sessizce boş
 * sonuç döndürebildiğinden, başarısız olursa ağ üzerinden OpenStreetMap Nominatim ile yedek
 * çözümleme yapılır. İkisi de başarısız olursa null döner; kullanıcı konumu manuel seçebilir.
 */
suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): GeoArea =
    withContext(Dispatchers.IO) {
        val deviceResult = deviceGeocode(context, latitude, longitude)
        if (deviceResult?.il != null) deviceResult else networkGeocode(latitude, longitude)
    }

private fun deviceGeocode(context: Context, latitude: Double, longitude: Double): GeoArea? =
    try {
        if (!Geocoder.isPresent()) null
        else {
            val geocoder = Geocoder(context, Locale("tr", "TR"))
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocation(latitude, longitude, 1)
            val address = results?.firstOrNull()
            if (address == null) null
            else GeoArea(il = address.adminArea, ilce = address.subAdminArea ?: address.locality)
        }
    } catch (e: Exception) {
        null
    }

private fun networkGeocode(latitude: Double, longitude: Double): GeoArea =
    try {
        val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2" +
            "&lat=$latitude&lon=$longitude&zoom=14&addressdetails=1&accept-language=tr"
        require(url.startsWith("https://")) { "Sadece HTTPS istekleri desteklenir: $url" }

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "EzanVakti-Android/1.0 (Turkce namaz vakti uygulamasi)")
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("HTTP $code: $url")
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val address = JSONObject(body).optJSONObject("address")
            if (address == null) GeoArea(null, null)
            else GeoArea(
                il = address.optString("state").ifBlank { null },
                // Türkiye'de ilçe seviyesi OSM'de genelde city_district/district olarak etiketlenir;
                // town/suburb/county daha çok mahalle ya da farklı ülke şemalarına ait, son çare olarak kullanılır.
                ilce = address.optString("city_district").ifBlank { null }
                    ?: address.optString("district").ifBlank { null }
                    ?: address.optString("county").ifBlank { null }
                    ?: address.optString("town").ifBlank { null }
                    ?: address.optString("suburb").ifBlank { null }
            )
        } finally {
            connection.disconnect()
        }
    } catch (e: Exception) {
        GeoArea(null, null)
    }
