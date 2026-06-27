package com.metehanyl.ezanvakti.data

import android.content.Context
import com.metehanyl.ezanvakti.data.model.PrayerBundle
import com.metehanyl.ezanvakti.data.model.RefreshResult
import com.metehanyl.ezanvakti.data.model.SelectedLocation
import com.metehanyl.ezanvakti.util.trKey

class PrayerRepository(private val context: Context) {

    private val cache = PrayerCache(context)
    private val settings = AppSettings(context)
    private val locationProvider = DeviceLocationProvider(context)

    fun getCachedBundle(): PrayerBundle? = cache.load()

    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()

    fun getManualLocation(): SelectedLocation? = settings.getManualLocation()

    fun setManualLocation(location: SelectedLocation?) = settings.setManualLocation(location)

    suspend fun refresh(): RefreshResult {
        val location = resolveLocation()
            ?: return RefreshResult.Failure("Konum belirlenemedi. Konum izni verin ya da şehrinizi manuel seçin.")

        return try {
            val days = DiyanetApi.getPrayerTimes(location.ilceId)
            if (days.isEmpty()) {
                RefreshResult.Failure("Diyanet sunucusundan vakit verisi alınamadı.")
            } else {
                val bundle = PrayerBundle(
                    sehirId = location.sehirId,
                    sehirAdi = location.sehirAdi,
                    ilceId = location.ilceId,
                    ilceAdi = location.ilceAdi,
                    days = days,
                    fetchedAtEpochMillis = System.currentTimeMillis()
                )
                cache.save(bundle)
                RefreshResult.Success(bundle)
            }
        } catch (e: Exception) {
            RefreshResult.Failure("İnternete bağlanılamadı. Son kaydedilen vakitler gösteriliyor.")
        }
    }

    private suspend fun resolveLocation(): SelectedLocation? {
        settings.getManualLocation()?.let { return it }

        val location = locationProvider.getCurrentLocation() ?: return null
        val area = reverseGeocode(context, location.latitude, location.longitude)
        val il = area.il ?: return null
        return resolveCityAndDistrict(il, area.ilceCandidates)
    }

    private suspend fun resolveCityAndDistrict(il: String, ilceCandidates: List<String>): SelectedLocation? {
        val ilKey = trKey(il)
        val cities = DiyanetApi.getCities()
        val city = cities.firstOrNull { trKey(it.name) == ilKey }
            ?: cities.firstOrNull { trKey(it.name).contains(ilKey) || ilKey.contains(trKey(it.name)) }
            ?: return null

        val districts = DiyanetApi.getDistricts(city.id)
        if (districts.isEmpty()) return null

        val candidateKeys = ilceCandidates.map { trKey(it) }.filter { it.isNotBlank() }
        val district = candidateKeys.firstNotNullOfOrNull { key ->
            districts.firstOrNull { trKey(it.name) == key }
        } ?: candidateKeys.firstNotNullOfOrNull { key ->
            districts.firstOrNull { trKey(it.name).contains(key) || key.contains(trKey(it.name)) }
        } ?: return null

        return SelectedLocation(
            sehirId = city.id,
            sehirAdi = city.name,
            ilceId = district.id,
            ilceAdi = district.name
        )
    }
}
