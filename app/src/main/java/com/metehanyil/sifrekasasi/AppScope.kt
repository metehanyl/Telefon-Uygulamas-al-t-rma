package com.metehanyil.sifrekasasi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * A coroutine scope that lives for as long as the process does, independent
 * of any single Activity's lifecycle. Used for work that must run to
 * completion even if the screen that started it gets recreated or finished
 * in the meantime - e.g. a bulk CSV import that keeps going while/after the
 * system file picker is shown, or across a screen rotation.
 */
object AppScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
