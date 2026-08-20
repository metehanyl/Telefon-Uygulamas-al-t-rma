package com.metehanyil.sifrekasasi

/**
 * Tracks whether the user has unlocked the vault with the master password
 * during the current process lifetime. Reset to false whenever the whole
 * app goes to the background, so the master password is required again.
 */
object AppLockState {
    @Volatile
    var isUnlocked: Boolean = false
}
