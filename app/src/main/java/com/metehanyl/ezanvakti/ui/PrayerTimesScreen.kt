package com.metehanyl.ezanvakti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.metehanyl.ezanvakti.PrayerUiState
import com.metehanyl.ezanvakti.R
import com.metehanyl.ezanvakti.data.DiyanetApi
import com.metehanyl.ezanvakti.data.model.ESMA_UL_HUSNA
import com.metehanyl.ezanvakti.data.model.PrayerBundle
import com.metehanyl.ezanvakti.ui.theme.CrescentGold
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    uiState: PrayerUiState,
    onRefresh: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onOpenLocationPicker: () -> Unit,
    onToggleDarkTheme: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(28.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_mosque),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_crescent),
                                contentDescription = null,
                                tint = CrescentGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(stringResource(R.string.screen_title))
                    }
                },
                actions = {
                    IconButton(onClick = onToggleDarkTheme) {
                        Icon(
                            if (uiState.darkThemeEnabled) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = stringResource(R.string.action_toggle_theme)
                        )
                    }
                    IconButton(onClick = onOpenLocationPicker) {
                        Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.action_change_location))
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                }
            )
        }
    ) { padding ->
        val backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val bundle = uiState.bundle

            item { CountdownCard(bundle) }

            item { LocationRow(bundle) }

            if (uiState.isOffline || (!uiState.isShowingExactToday && bundle != null)) {
                item { OfflineBanner(isStale = !uiState.isShowingExactToday) }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                when {
                    bundle != null -> PrayerTimesCard(bundle)
                    uiState.isLoading -> LoadingRow()
                    else -> EmptyStateCard(onRefresh)
                }
            }

            item { NotificationToggleRow(uiState.notificationsEnabled, onToggleNotifications) }

            item { DataSourceFooter() }

            item { EsmaulHusnaHeader() }

            items(ESMA_UL_HUSNA, key = { it.no }) { esma ->
                EsmaCard(esma)
            }
        }
    }
}

@Composable
private fun CountdownCard(bundle: PrayerBundle?) {
    if (bundle == null) return
    val (day, isToday) = bundle.todayOrClosest()
    if (!isToday) return

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    val vakitler = day.toList()
    val nextIndex = remember(vakitler, nowMillis) { nextVakitIndex(vakitler) }
    val (nextName, nextTime) = vakitler[nextIndex]
    val remainingText = remember(vakitler, nowMillis) { remainingTimeText(vakitler, nextIndex) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_mosque),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_crescent),
                    contentDescription = null,
                    tint = CrescentGold,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                text = stringResource(R.string.countdown_label, nextName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = remainingText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.next_vakit_label, nextName, nextTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun LocationRow(bundle: PrayerBundle?) {
    if (bundle == null) return
    Text(
        text = stringResource(R.string.location_label, bundle.sehirAdi, bundle.ilceAdi),
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = stringResource(R.string.last_updated_label, formatTimestamp(bundle.fetchedAtEpochMillis)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun OfflineBanner(isStale: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Text(
                text = stringResource(if (isStale) R.string.stale_banner else R.string.offline_banner),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LoadingRow() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.loading_label), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyStateCard(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.empty_state_label),
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
private fun PrayerTimesCard(bundle: PrayerBundle) {
    val (day, _) = bundle.todayOrClosest()
    val vakitler = day.toList()
    val nextIndex = remember(vakitler) { nextVakitIndex(vakitler) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            vakitler.forEachIndexed { index, (name, time) ->
                VakitRow(name = name, time = time, highlighted = index == nextIndex)
            }
        }
    }
}

@Composable
private fun VakitRow(name: String, time: String, highlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
        Text(text = time, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun NotificationToggleRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null)
            Column {
                Text(stringResource(R.string.notifications_toggle_title), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.notifications_toggle_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun DataSourceFooter() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.data_source_label, DiyanetApi.DATA_SOURCE_HOST),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun nextVakitIndex(vakitler: List<Pair<String, String>>): Int {
    val now = Calendar.getInstance()
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val minutes = vakitler.map { (_, time) ->
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        hour * 60 + minute
    }
    val index = minutes.indexOfFirst { it > nowMinutes }
    return if (index == -1) 0 else index
}

private fun remainingTimeText(vakitler: List<Pair<String, String>>, nextIndex: Int): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance()
    val (_, time) = vakitler[nextIndex]
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    target.set(Calendar.HOUR_OF_DAY, hour)
    target.set(Calendar.MINUTE, minute)
    target.set(Calendar.SECOND, 0)
    target.set(Calendar.MILLISECOND, 0)
    if (target.before(now) || target == now) {
        target.add(Calendar.DAY_OF_YEAR, 1)
    }
    val totalSeconds = ((target.timeInMillis - now.timeInMillis) / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale("tr", "TR"), "%02d:%02d:%02d", hours, minutes, seconds)
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(epochMillis))
