package com.metehanyl.ezanvakti.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.metehanyl.ezanvakti.data.AppSettings
import com.metehanyl.ezanvakti.data.PrayerCache

/** Cihaz yeniden başladığında kurulu alarmlar silindiği için bugünün alarmlarını yeniden kurar. */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val settings = AppSettings(context)
        if (!settings.notificationsEnabled) return

        val bundle = PrayerCache(context).load() ?: return
        PrayerNotificationScheduler(context).scheduleFromBundle(bundle)
    }
}
