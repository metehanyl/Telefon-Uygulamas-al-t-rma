package com.metehanyl.ezanvakti.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.metehanyl.ezanvakti.data.model.PrayerBundle
import java.util.Calendar

/**
 * Bugünün vakitleri için cihazda alarm kurar. Sadece elimizdeki veri bugüne ait ise
 * (önbellek eskiyse değil) bildirim planlanır.
 */
class PrayerNotificationScheduler(private val context: Context) {

    fun scheduleFromBundle(bundle: PrayerBundle) {
        cancelAll()
        val (day, isToday) = bundle.todayOrClosest()
        if (!isToday) return

        val now = System.currentTimeMillis()
        day.toList().forEachIndexed { index, (vakitAdi, time) ->
            val triggerAtMillis = timeTodayMillis(time) ?: return@forEachIndexed
            if (triggerAtMillis <= now) return@forEachIndexed
            scheduleAlarm(requestCode = index, triggerAtMillis = triggerAtMillis, vakitAdi = vakitAdi)
        }
    }

    fun cancelAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (requestCode in 0 until VAKIT_COUNT) {
            alarmManager.cancel(pendingIntentFor(requestCode))
        }
    }

    private fun scheduleAlarm(requestCode: Int, triggerAtMillis: Long, vakitAdi: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntentFor(requestCode, vakitAdi)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun pendingIntentFor(requestCode: Int, vakitAdi: String? = null): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            vakitAdi?.let { putExtra(PrayerAlarmReceiver.EXTRA_VAKIT_ADI, it) }
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun timeTodayMillis(hhmm: String): Long? {
        val parts = hhmm.split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    companion object {
        const val VAKIT_COUNT = 6
    }
}
