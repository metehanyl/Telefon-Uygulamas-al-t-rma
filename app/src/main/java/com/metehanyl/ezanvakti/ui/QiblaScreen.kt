package com.metehanyl.ezanvakti.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.metehanyl.ezanvakti.R
import com.metehanyl.ezanvakti.data.DeviceLocationProvider
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val KAABA_LATITUDE = 21.4224779
private const val KAABA_LONGITUDE = 39.8251832

private fun bearingToKaaba(lat: Double, lon: Double): Double {
    val lat1 = Math.toRadians(lat)
    val lat2 = Math.toRadians(KAABA_LATITUDE)
    val deltaLon = Math.toRadians(KAABA_LONGITUDE - lon)

    val y = sin(deltaLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
    val bearing = Math.toDegrees(atan2(y, x))
    return (bearing + 360) % 360
}

@Composable
fun QiblaScreen(onRequestLocationPermission: () -> Unit) {
    val context = LocalContext.current
    val locationProvider = remember { DeviceLocationProvider(context) }
    var hasPermission by remember { mutableStateOf(locationProvider.hasLocationPermission()) }
    var qiblaBearing by remember { mutableStateOf<Double?>(null) }
    var azimuth by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        hasPermission = locationProvider.hasLocationPermission()
        onDispose { }
    }

    if (!hasPermission) {
        QiblaPermissionRequired(onRequestLocationPermission)
        return
    }

    LaunchedEffect(hasPermission) {
        val location = locationProvider.getCurrentLocation()
        if (location != null) {
            qiblaBearing = bearingToKaaba(location.latitude, location.longitude)
        }
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var lastAccel: FloatArray? = null
        var lastMagnetic: FloatArray? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        azimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        lastAccel = event.values.clone()
                        updateFromAccelMagnetic(lastAccel, lastMagnetic, rotationMatrix, orientation) { azimuth = it }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        lastMagnetic = event.values.clone()
                        updateFromAccelMagnetic(lastAccel, lastMagnetic, rotationMatrix, orientation) { azimuth = it }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose { sensorManager.unregisterListener(listener) }
    }

    val bearing = qiblaBearing
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.qibla_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.qibla_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        if (bearing == null) {
            Text(stringResource(R.string.qibla_locating), style = MaterialTheme.typography.bodyMedium)
        } else {
            val rotation = (((bearing - azimuth) + 360) % 360).toFloat()
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Navigation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(120.dp)
                        .rotate(rotation)
                )
            }
            Text(
                text = stringResource(R.string.qibla_degree_label, bearing.toFloat()),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

private inline fun updateFromAccelMagnetic(
    accel: FloatArray?,
    magnetic: FloatArray?,
    rotationMatrix: FloatArray,
    orientation: FloatArray,
    onAzimuth: (Float) -> Unit
) {
    if (accel == null || magnetic == null) return
    val success = SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnetic)
    if (success) {
        SensorManager.getOrientation(rotationMatrix, orientation)
        onAzimuth((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
    }
}

@Composable
private fun QiblaPermissionRequired(onRequestLocationPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.ExploreOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(R.string.qibla_permission_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )
        Button(onClick = onRequestLocationPermission) {
            Text(stringResource(R.string.qibla_permission_button))
        }
    }
}
