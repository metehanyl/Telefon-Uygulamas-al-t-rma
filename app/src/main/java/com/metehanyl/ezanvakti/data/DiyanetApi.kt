package com.metehanyl.ezanvakti.data

import com.metehanyl.ezanvakti.data.model.CityOption
import com.metehanyl.ezanvakti.data.model.DistrictOption
import com.metehanyl.ezanvakti.data.model.PrayerDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Diyanet İşleri Başkanlığı'nın namazvakitleri.diyanet.gov.tr verisini ayna olarak sunan
 * topluluk servisi (ezanvakti.emushaf.net) üzerinden il/ilçe ve vakit bilgisi çeker.
 */
object DiyanetApi {

    private const val BASE_URL = "https://ezanvakti.emushaf.net"
    private const val TURKEY_COUNTRY_ID = "2"

    suspend fun getCities(): List<CityOption> = withContext(Dispatchers.IO) {
        val json = httpGet("$BASE_URL/sehirler/$TURKEY_COUNTRY_ID")
        val array = JSONArray(json)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(CityOption(id = obj.getString("SehirID"), name = obj.getString("SehirAdi")))
            }
        }
    }

    suspend fun getDistricts(cityId: String): List<DistrictOption> = withContext(Dispatchers.IO) {
        val json = httpGet("$BASE_URL/ilceler/$cityId")
        val array = JSONArray(json)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(DistrictOption(id = obj.getString("IlceID"), name = obj.getString("IlceAdi")))
            }
        }
    }

    suspend fun getPrayerTimes(districtId: String): List<PrayerDay> = withContext(Dispatchers.IO) {
        val json = httpGet("$BASE_URL/vakitler/$districtId")
        val array = JSONArray(json)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    PrayerDay(
                        tarih = obj.getString("MiladiTarihKisa"),
                        imsak = obj.getString("Imsak"),
                        gunes = obj.getString("Gunes"),
                        ogle = obj.getString("Ogle"),
                        ikindi = obj.getString("Ikindi"),
                        aksam = obj.getString("Aksam"),
                        yatsi = obj.getString("Yatsi")
                    )
                )
            }
        }
    }

    private fun httpGet(urlString: String): String {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/json")
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("HTTP $code: $urlString")
            }
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
