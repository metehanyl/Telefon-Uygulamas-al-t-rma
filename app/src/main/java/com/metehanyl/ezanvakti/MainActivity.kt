package com.metehanyl.ezanvakti

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.metehanyl.ezanvakti.ui.LocationPickerDialog
import com.metehanyl.ezanvakti.ui.PrayerTimesScreen
import com.metehanyl.ezanvakti.ui.theme.EzanVaktiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PrayerTimesViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.refresh()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationsEnabled(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermissionIfNeeded()

        setContent {
            EzanVaktiTheme {
                val uiState by viewModel.uiState.collectAsState()
                val pickerState by viewModel.pickerState.collectAsState()

                PrayerTimesScreen(
                    uiState = uiState,
                    onRefresh = { requestLocationPermissionIfNeeded() },
                    onToggleNotifications = { enabled -> onToggleNotifications(enabled) },
                    onOpenLocationPicker = { viewModel.openLocationPicker() }
                )

                if (pickerState.isOpen) {
                    LocationPickerDialog(
                        state = pickerState,
                        hasManualLocation = uiState.manualLocation != null,
                        onDismiss = { viewModel.closeLocationPicker() },
                        onSelectCity = { viewModel.selectCity(it) },
                        onSelectDistrict = { viewModel.selectDistrict(it) },
                        onConfirm = { viewModel.confirmManualLocation() },
                        onUseAutomaticLocation = { viewModel.useAutomaticLocation() }
                    )
                }
            }
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        val hasFine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.refresh()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun onToggleNotifications(enabled: Boolean) {
        if (!enabled) {
            viewModel.setNotificationsEnabled(false)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        viewModel.setNotificationsEnabled(true)
        requestExactAlarmPermissionIfNeeded()
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
            )
        }
    }
}
