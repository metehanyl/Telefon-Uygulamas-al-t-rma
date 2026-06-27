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
        val location = when (val lookup = resolveLocation()) {
            is LocationLookup.Success -> lookup.location
            is LocationLookup.Failure ->
                return RefreshResult.Failure("Konum belirlenemedi (${lookup.reason}). Konum izni verin ya da şehrinizi manuel seçin.")
        }

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

    private suspend fun resolveLocation(): LocationLookup {
        settings.getManualLocation()?.let { return LocationLookup.Success(it) }

        if (!locationProvider.hasLocationPermission()) {
            return LocationLookup.Failure("konum izni verilmedi")
        }
        val location = locationProvider.getCurrentLocation()
            ?: return LocationLookup.Failure("GPS/ağ konumu alınamadı")

        val area = reverseGeocode(context, location.latitude, location.longitude)
        val il = area.il
            ?: return LocationLookup.Failure("konum coğrafi olarak çözümlenemedi, il bulunamadı")

        return try {
            resolveCityAndDistrict(il, area.ilceCandidates)
        } catch (e: Exception) {
            LocationLookup.Failure("Diyanet şehir/ilçe listesi alınamadı: ${e.message}")
        }
    }

    private suspend fun resolveCityAndDistrict(il: String, ilceCandidates: List<String>): LocationLookup {
        val ilKey = trKey(il)
        val cities = DiyanetApi.getCities()
        val city = cities.firstOrNull { trKey(it.name) == ilKey }
            ?: cities.firstOrNull { trKey(it.name).contains(ilKey) || ilKey.contains(trKey(it.name)) }
            ?: return LocationLookup.Failure("şehir bulunamadı: $il")

        val districts = DiyanetApi.getDistricts(city.id)
        if (districts.isEmpty()) return LocationLookup.Failure("${city.name} için ilçe listesi boş döndü")

        val candidateKeys = ilceCandidates.map { trKey(it) }.filter { it.isNotBlank() }
        val district = candidateKeys.firstNotNullOfOrNull { key ->
            districts.firstOrNull { trKey(it.name) == key }
        } ?: candidateKeys.firstNotNullOfOrNull { key ->
            districts.firstOrNull { trKey(it.name).contains(key) || key.contains(trKey(it.name)) }
        }

        if (district == null) {
            val candidatesText = if (ilceCandidates.isEmpty()) "aday yok" else ilceCandidates.joinToString("/")
            return LocationLookup.Failure("$il ilinde ilçe eşleşmedi, adaylar: $candidatesText")
        }

        return LocationLookup.Success(
            SelectedLocation(
                sehirId = city.id,
                sehirAdi = city.name,
                ilceId = district.id,
                ilceAdi = district.name
            )
        )
    }

    private sealed class LocationLookup {
        data class Success(val location: SelectedLocation) : LocationLookup()
        data class Failure(val reason: String) : LocationLookup()
    }
}
