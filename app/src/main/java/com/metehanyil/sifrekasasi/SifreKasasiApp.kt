package com.metehanyil.sifrekasasi

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Locks the vault again the moment the whole app moves to the background
 * (not just a single activity), so switching apps or hitting Home always
 * requires the master password on return.
 */
class SifreKasasiApp : Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        AppLockState.isUnlocked = false
    }
}
