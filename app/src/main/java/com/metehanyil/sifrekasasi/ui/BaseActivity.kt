package com.metehanyil.sifrekasasi.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.metehanyil.sifrekasasi.AppLockState

/**
 * Any screen that shows vault data extends this so it re-checks the lock
 * state every time it becomes visible - including when the app is resumed
 * from the background, which is when [AppLockState.isUnlocked] gets reset.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        if (!AppLockState.isUnlocked) {
            startActivity(Intent(this, LockActivity::class.java))
            finish()
        }
    }
}
