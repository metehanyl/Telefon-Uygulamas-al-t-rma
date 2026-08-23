package com.metehanyl.ezanvakti.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tek bir günün vakitleri. [tarih] formatı "dd.MM.yyyy" (Diyanet API formatı). */
data class PrayerDay(
    val tarih: String,
    val imsak: String,
    val gunes: String,
    val ogle: String,
    val ikindi: String,
    val aksam: String,
    val yatsi: String
) {
    fun toList(): List<Pair<String, String>> = listOf(
        "İmsak" to imsak,
        "Güneş" to gunes,
        "Öğle" to ogle,
        "İkindi" to ikindi,
        "Akşam" to aksam,
        "Yatsı" to yatsi
    )
}

data class CityOption(val id: String, val name: String)

data class DistrictOption(val id: String, val name: String)

/** Seçilmiş il/ilçe bilgisi (otomatik konumdan ya da manuel seçimden gelebilir). */
data class SelectedLocation(
    val sehirId: String,
    val sehirAdi: String,
    val ilceId: String,
    val ilceAdi: String
)

/** Sunucudan çekilip diskte saklanan vakit verisi paketi. */
data class PrayerBundle(
    val sehirId: String,
    val sehirAdi: String,
    val ilceId: String,
    val ilceAdi: String,
    val days: List<PrayerDay>,
    val fetchedAtEpochMillis: Long
) {
    fun findDay(dateStr: String): PrayerDay? = days.firstOrNull { it.tarih == dateStr }

    /** Bugünün tarihiyle eşleşen günü, yoksa elimizdeki en yakın günü döner. */
    fun todayOrClosest(): Pair<PrayerDay, Boolean> {
        val todayStr = todayDateString()
        val exact = findDay(todayStr)
        if (exact != null) return exact to true
        val fallback = days.lastOrNull() ?: days.first()
        return fallback to false
    }

    companion object {
        fun todayDateString(): String =
            SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date())
    }
}

sealed class RefreshResult {
    data class Success(val bundle: PrayerBundle) : RefreshResult()
    data class Failure(val message: String) : RefreshResult()
}
