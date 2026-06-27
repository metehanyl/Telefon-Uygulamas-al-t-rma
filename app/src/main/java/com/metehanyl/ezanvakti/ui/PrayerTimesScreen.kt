package com.metehanyl.ezanvakti.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metehanyl.ezanvakti.PrayerUiState
import com.metehanyl.ezanvakti.R
import com.metehanyl.ezanvakti.data.DiyanetApi
import com.metehanyl.ezanvakti.data.model.ESMA_UL_HUSNA
import com.metehanyl.ezanvakti.data.model.PrayerBundle
import com.metehanyl.ezanvakti.ui.theme.Green700
import com.metehanyl.ezanvakti.ui.theme.LightningYellow
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
    onToggleDarkTheme: () -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_crescent_logo),
                            contentDescription = null,
                            tint = Green700,
                            modifier = Modifier.size(22.dp)
                        )
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.tab_vakitler)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.tab_kible)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.tab_esma)) }
                )
            }

            when (selectedTab) {
                0 -> VakitlerTabContent(uiState, onRefresh, onToggleNotifications)
                1 -> QiblaScreen(onRequestLocationPermission = onRequestLocationPermission)
                else -> EsmaTabContent()
            }
        }
    }
}

@Composable
private fun VakitlerTabContent(
    uiState: PrayerUiState,
    onRefresh: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val bundle = uiState.bundle

        item { CountdownCard(bundle) }

        item { LocationRow(bundle) }

        if (bundle != null) {
            item { SectionDivider() }
        }

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
    }
}

@Composable
private fun EsmaTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { EsmaulHusnaHeader() }

        items(ESMA_UL_HUSNA, key = { it.no }) { esma ->
            EsmaCard(esma)
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
    val (hours, minutes, seconds) = remember(vakitler, nowMillis) { remainingTimeParts(vakitler, nextIndex) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.countdown_section_label).uppercase(Locale("tr", "TR")),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = nextName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = nextTime,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CountdownBox(value = hours, label = stringResource(R.string.unit_hours))
                    ColonSeparator()
                    CountdownBox(value = minutes, label = stringResource(R.string.unit_minutes))
                    ColonSeparator()
                    CountdownBox(value = seconds, label = stringResource(R.string.unit_seconds))
                }
            }
            Icon(
                Icons.Filled.Bolt,
                contentDescription = null,
                tint = LightningYellow,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 14.dp)
                    .size(18.dp)
            )
        }
    }
}

@Composable
private fun ColonSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun CountdownBox(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(48.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Text(
            text = label.uppercase(Locale("tr", "TR")),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationRow(bundle: PrayerBundle?) {
    if (bundle == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        Image(
            painter = painterResource(R.drawable.ic_yildirim_logo),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun SectionDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
        Icon(
            painter = painterResource(R.drawable.ic_islamic_star),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
    }
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
    val currentIndex = remember(vakitler, nextIndex) { (nextIndex - 1 + vakitler.size) % vakitler.size }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        vakitler.forEachIndexed { index, (name, time) ->
            VakitRow(
                name = name,
                time = time,
                isCurrent = index == currentIndex,
                isNext = index == nextIndex
            )
        }
    }
}

private data class VakitVisual(
    val icon: ImageVector,
    val subtitleRes: Int,
    val background: Brush,
    val iconTint: Color
)

private fun vakitVisual(name: String): VakitVisual = when (name) {
    "İmsak" -> VakitVisual(
        icon = Icons.Filled.NightlightRound,
        subtitleRes = R.string.vakit_subtitle_imsak,
        background = Brush.linearGradient(listOf(Color(0xFF1B2A4A), Color(0xFF111A30))),
        iconTint = Color(0xFFB9C6E8)
    )
    "Güneş" -> VakitVisual(
        icon = Icons.Filled.WbTwilight,
        subtitleRes = R.string.vakit_subtitle_gunes,
        background = Brush.linearGradient(listOf(Color(0xFFFF8A65), Color(0xFFFFC371))),
        iconTint = Color(0xFF3A1E0A)
    )
    "Öğle" -> VakitVisual(
        icon = Icons.Filled.WbSunny,
        subtitleRes = R.string.vakit_subtitle_ogle,
        background = Brush.linearGradient(listOf(Color(0xFF2F4671), Color(0xFF1B2A4A))),
        iconTint = Color(0xFFE8B94B)
    )
    "İkindi" -> VakitVisual(
        icon = Icons.Filled.WbCloudy,
        subtitleRes = R.string.vakit_subtitle_ikindi,
        background = Brush.linearGradient(listOf(Color(0xFF2F4671), Color(0xFF1B2A4A))),
        iconTint = Color(0xFFCBD6EE)
    )
    "Akşam" -> VakitVisual(
        icon = Icons.Filled.LocationCity,
        subtitleRes = R.string.vakit_subtitle_aksam,
        background = Brush.linearGradient(listOf(Color(0xFFB45B6B), Color(0xFF4B2A53))),
        iconTint = Color(0xFFFFD9E0)
    )
    else -> VakitVisual(
        icon = Icons.Filled.LocationCity,
        subtitleRes = R.string.vakit_subtitle_yatsi,
        background = Brush.linearGradient(listOf(Color(0xFF1A2238), Color(0xFF0A0F1E))),
        iconTint = Color(0xFF9FB3D9)
    )
}

@Composable
private fun VakitRow(name: String, time: String, isCurrent: Boolean, isNext: Boolean) {
    val visual = remember(name) { vakitVisual(name) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(visual.background, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(visual.icon, contentDescription = null, tint = visual.iconTint, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = stringResource(visual.subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = time, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                when {
                    isCurrent -> StatusBadge(text = stringResource(R.string.badge_now), filled = true)
                    isNext -> StatusBadge(text = stringResource(R.string.badge_next), filled = false)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, filled: Boolean) {
    val color = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(50)
    val badgeModifier = if (filled) {
        Modifier.background(color = color, shape = shape)
    } else {
        Modifier.border(BorderStroke(1.dp, color), shape)
    }
    Box(modifier = badgeModifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(
            text = text.uppercase(Locale("tr", "TR")),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (filled) MaterialTheme.colorScheme.background else color
        )
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
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
            Text(
                text = stringResource(R.string.diyanet_method_footer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 32.dp)
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

private fun remainingTimeParts(vakitler: List<Pair<String, String>>, nextIndex: Int): Triple<String, String, String> {
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
    val locale = Locale("tr", "TR")
    return Triple(
        String.format(locale, "%02d", hours),
        String.format(locale, "%02d", minutes),
        String.format(locale, "%02d", seconds)
    )
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(epochMillis))
