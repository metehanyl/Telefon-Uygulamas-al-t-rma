package com.metehanyl.ezanvakti.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metehanyl.ezanvakti.ui.theme.Green700
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// ─── Veri modelleri ──────────────────────────────────────────────────────────

data class SurahMeta(
    val number: Int,
    val nameTr: String,
    val nameAr: String,
    val verseCount: Int,
    val revelation: String   // Mekkî / Medenî
)

data class QuranVerse(
    val numberInSurah: Int,
    val turkish: String
)

data class SurahContent(
    val meta: SurahMeta,
    val verses: List<QuranVerse>
)

enum class AudioPlayerState { IDLE, LOADING, PLAYING, PAUSED, ERROR }

// ─── 114 Sure meta verisi ────────────────────────────────────────────────────

val ALL_SURAHS: List<SurahMeta> = listOf(
    SurahMeta(1,   "Fâtiha",       "الفاتحة",    7,   "Mekkî"),
    SurahMeta(2,   "Bakara",       "البقرة",     286,  "Medenî"),
    SurahMeta(3,   "Âl-i İmrân",  "آل عمران",  200,  "Medenî"),
    SurahMeta(4,   "Nisâ",         "النساء",     176,  "Medenî"),
    SurahMeta(5,   "Mâide",        "المائدة",    120,  "Medenî"),
    SurahMeta(6,   "En'âm",        "الأنعام",    165,  "Mekkî"),
    SurahMeta(7,   "A'râf",        "الأعراف",    206,  "Mekkî"),
    SurahMeta(8,   "Enfâl",        "الأنفال",    75,   "Medenî"),
    SurahMeta(9,   "Tevbe",        "التوبة",     129,  "Medenî"),
    SurahMeta(10,  "Yûnus",        "يونس",       109,  "Mekkî"),
    SurahMeta(11,  "Hûd",          "هود",        123,  "Mekkî"),
    SurahMeta(12,  "Yûsuf",        "يوسف",       111,  "Mekkî"),
    SurahMeta(13,  "Ra'd",         "الرعد",      43,   "Medenî"),
    SurahMeta(14,  "İbrâhîm",      "إبراهيم",    52,   "Mekkî"),
    SurahMeta(15,  "Hicr",         "الحجر",      99,   "Mekkî"),
    SurahMeta(16,  "Nahl",         "النحل",      128,  "Mekkî"),
    SurahMeta(17,  "İsrâ",         "الإسراء",    111,  "Mekkî"),
    SurahMeta(18,  "Kehf",         "الكهف",      110,  "Mekkî"),
    SurahMeta(19,  "Meryem",       "مريم",       98,   "Mekkî"),
    SurahMeta(20,  "Tâ-Hâ",        "طه",         135,  "Mekkî"),
    SurahMeta(21,  "Enbiyâ",       "الأنبياء",   112,  "Mekkî"),
    SurahMeta(22,  "Hac",          "الحج",       78,   "Medenî"),
    SurahMeta(23,  "Mü'minûn",     "المؤمنون",   118,  "Mekkî"),
    SurahMeta(24,  "Nûr",          "النور",      64,   "Medenî"),
    SurahMeta(25,  "Furkân",       "الفرقان",    77,   "Mekkî"),
    SurahMeta(26,  "Şu'arâ",       "الشعراء",    227,  "Mekkî"),
    SurahMeta(27,  "Neml",         "النمل",      93,   "Mekkî"),
    SurahMeta(28,  "Kasas",        "القصص",      88,   "Mekkî"),
    SurahMeta(29,  "Ankebût",      "العنكبوت",   69,   "Mekkî"),
    SurahMeta(30,  "Rûm",          "الروم",      60,   "Mekkî"),
    SurahMeta(31,  "Lokmân",       "لقمان",      34,   "Mekkî"),
    SurahMeta(32,  "Secde",        "السجدة",     30,   "Mekkî"),
    SurahMeta(33,  "Ahzâb",        "الأحزاب",    73,   "Medenî"),
    SurahMeta(34,  "Sebe",         "سبإ",        54,   "Mekkî"),
    SurahMeta(35,  "Fâtır",        "فاطر",       45,   "Mekkî"),
    SurahMeta(36,  "Yâsîn",        "يس",         83,   "Mekkî"),
    SurahMeta(37,  "Sâffât",       "الصافات",    182,  "Mekkî"),
    SurahMeta(38,  "Sâd",          "ص",          88,   "Mekkî"),
    SurahMeta(39,  "Zümer",        "الزمر",      75,   "Mekkî"),
    SurahMeta(40,  "Mü'min",       "غافر",       85,   "Mekkî"),
    SurahMeta(41,  "Fussilet",     "فصلت",       54,   "Mekkî"),
    SurahMeta(42,  "Şûrâ",         "الشورى",     53,   "Mekkî"),
    SurahMeta(43,  "Zuhruf",       "الزخرف",     89,   "Mekkî"),
    SurahMeta(44,  "Duhân",        "الدخان",     59,   "Mekkî"),
    SurahMeta(45,  "Câsiye",       "الجاثية",    37,   "Mekkî"),
    SurahMeta(46,  "Ahkâf",        "الأحقاف",    35,   "Mekkî"),
    SurahMeta(47,  "Muhammed",     "محمد",       38,   "Medenî"),
    SurahMeta(48,  "Fetih",        "الفتح",      29,   "Medenî"),
    SurahMeta(49,  "Hucurât",      "الحجرات",    18,   "Medenî"),
    SurahMeta(50,  "Kâf",          "ق",          45,   "Mekkî"),
    SurahMeta(51,  "Zâriyât",      "الذاريات",   60,   "Mekkî"),
    SurahMeta(52,  "Tûr",          "الطور",      49,   "Mekkî"),
    SurahMeta(53,  "Necm",         "النجم",      62,   "Mekkî"),
    SurahMeta(54,  "Kamer",        "القمر",      55,   "Mekkî"),
    SurahMeta(55,  "Rahmân",       "الرحمن",     78,   "Medenî"),
    SurahMeta(56,  "Vâkıa",        "الواقعة",    96,   "Mekkî"),
    SurahMeta(57,  "Hadîd",        "الحديد",     29,   "Medenî"),
    SurahMeta(58,  "Mücâdele",     "المجادلة",   22,   "Medenî"),
    SurahMeta(59,  "Haşr",         "الحشر",      24,   "Medenî"),
    SurahMeta(60,  "Mümtehine",    "الممتحنة",   13,   "Medenî"),
    SurahMeta(61,  "Saf",          "الصف",       14,   "Medenî"),
    SurahMeta(62,  "Cum'a",        "الجمعة",     11,   "Medenî"),
    SurahMeta(63,  "Münâfikûn",    "المنافقون",  11,   "Medenî"),
    SurahMeta(64,  "Teğâbün",      "التغابن",    18,   "Medenî"),
    SurahMeta(65,  "Talâk",        "الطلاق",     12,   "Medenî"),
    SurahMeta(66,  "Tahrîm",       "التحريم",    12,   "Medenî"),
    SurahMeta(67,  "Mülk",         "الملك",      30,   "Mekkî"),
    SurahMeta(68,  "Kalem",        "القلم",      52,   "Mekkî"),
    SurahMeta(69,  "Hâkka",        "الحاقة",     52,   "Mekkî"),
    SurahMeta(70,  "Meâric",       "المعارج",    44,   "Mekkî"),
    SurahMeta(71,  "Nûh",          "نوح",        28,   "Mekkî"),
    SurahMeta(72,  "Cin",          "الجن",       28,   "Mekkî"),
    SurahMeta(73,  "Müzzemmil",    "المزمل",     20,   "Mekkî"),
    SurahMeta(74,  "Müddessir",    "المدثر",     56,   "Mekkî"),
    SurahMeta(75,  "Kıyâme",       "القيامة",    40,   "Mekkî"),
    SurahMeta(76,  "İnsân",        "الإنسان",    31,   "Medenî"),
    SurahMeta(77,  "Mürselât",     "المرسلات",   50,   "Mekkî"),
    SurahMeta(78,  "Nebe",         "النبأ",      40,   "Mekkî"),
    SurahMeta(79,  "Nâziât",       "النازعات",   46,   "Mekkî"),
    SurahMeta(80,  "Abese",        "عبس",        42,   "Mekkî"),
    SurahMeta(81,  "Tekvîr",       "التكوير",    29,   "Mekkî"),
    SurahMeta(82,  "İnfitâr",      "الانفطار",   19,   "Mekkî"),
    SurahMeta(83,  "Mutaffifîn",   "المطففين",   36,   "Mekkî"),
    SurahMeta(84,  "İnşikâk",      "الانشقاق",   25,   "Mekkî"),
    SurahMeta(85,  "Burûc",        "البروج",     22,   "Mekkî"),
    SurahMeta(86,  "Târık",        "الطارق",     17,   "Mekkî"),
    SurahMeta(87,  "A'lâ",         "الأعلى",     19,   "Mekkî"),
    SurahMeta(88,  "Gâşiye",       "الغاشية",    26,   "Mekkî"),
    SurahMeta(89,  "Fecr",         "الفجر",      30,   "Mekkî"),
    SurahMeta(90,  "Beled",        "البلد",      20,   "Mekkî"),
    SurahMeta(91,  "Şems",         "الشمس",      15,   "Mekkî"),
    SurahMeta(92,  "Leyl",         "الليل",      21,   "Mekkî"),
    SurahMeta(93,  "Duhâ",         "الضحى",      11,   "Mekkî"),
    SurahMeta(94,  "İnşirâh",      "الشرح",      8,    "Mekkî"),
    SurahMeta(95,  "Tîn",          "التين",      8,    "Mekkî"),
    SurahMeta(96,  "Alak",         "العلق",      19,   "Mekkî"),
    SurahMeta(97,  "Kadir",        "القدر",      5,    "Mekkî"),
    SurahMeta(98,  "Beyyine",      "البينة",     8,    "Medenî"),
    SurahMeta(99,  "Zilzâl",       "الزلزلة",    8,    "Medenî"),
    SurahMeta(100, "Âdiyât",       "العاديات",   11,   "Mekkî"),
    SurahMeta(101, "Kâria",        "القارعة",    11,   "Mekkî"),
    SurahMeta(102, "Tekâsür",      "التكاثر",    8,    "Mekkî"),
    SurahMeta(103, "Asr",          "العصر",      3,    "Mekkî"),
    SurahMeta(104, "Hümeze",       "الهمزة",     9,    "Mekkî"),
    SurahMeta(105, "Fîl",          "الفيل",      5,    "Mekkî"),
    SurahMeta(106, "Kureyş",       "قريش",       4,    "Mekkî"),
    SurahMeta(107, "Mâun",         "الماعون",    7,    "Mekkî"),
    SurahMeta(108, "Kevser",       "الكوثر",     3,    "Mekkî"),
    SurahMeta(109, "Kâfirûn",      "الكافرون",   6,    "Mekkî"),
    SurahMeta(110, "Nasr",         "النصر",      3,    "Medenî"),
    SurahMeta(111, "Tebbet",       "المسد",      5,    "Mekkî"),
    SurahMeta(112, "İhlâs",        "الإخلاص",    4,    "Mekkî"),
    SurahMeta(113, "Felak",        "الفلق",      5,    "Mekkî"),
    SurahMeta(114, "Nâs",          "الناس",      6,    "Mekkî")
)

// ─── Yardımcı fonksiyonlar ───────────────────────────────────────────────────

/** Mishary Alafasy kıraat ses dosyası (Islamic Network CDN) */
fun surahAudioUrl(number: Int): String =
    "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$number.mp3"

/** qurancdn API'nin döndürdüğü <sup>…</sup> dipnot etiketlerini temizler */
private fun stripHtml(text: String): String =
    text.replace(Regex("<[^>]+>"), "").trim()

/**
 * Mehmet Okuyan meal kimliği önbelleği.
 * quran.com API v4'teki çeviri ID'si uygulama ömrü boyunca tek kez çözümlenir.
 * Dinamik keşif başarısız olursa bilinen sabit ID kullanılır.
 */
private object OkuyanIdCache {
    @Volatile var id: Int? = null
    const val FALLBACK_ID = 112  // quran.com'da Mehmet Okuyan meali için bilinen ID
}

/**
 * quran.com API v4'ten Türkçe çeviri listesini çekerek
 * Mehmet Okuyan'a ait çeviri ID'sini bulur ve önbelleğe alır.
 * Keşif başarısız olursa FALLBACK_ID kullanılır.
 */
private suspend fun resolveOkuyanId(): Int =
    withContext(Dispatchers.IO) {
        OkuyanIdCache.id?.let { return@withContext it }
        try {
            val url = "https://api.quran.com/api/v4/resources/translations?language=tr"
            val conn = (URL(url).openConnection() as HttpURLConnection).also {
                it.connectTimeout = 10_000
                it.readTimeout = 15_000
                it.setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                OkuyanIdCache.id = OkuyanIdCache.FALLBACK_ID
                return@withContext OkuyanIdCache.FALLBACK_ID
            }
            val json = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            val arr = JSONObject(json).getJSONArray("translations")
            var found = -1
            for (i in 0 until arr.length()) {
                val t = arr.getJSONObject(i)
                val combined = t.optString("name", "") + "|" + t.optString("author_name", "")
                if (combined.contains("Okuyan", ignoreCase = true)) {
                    found = t.getInt("id")
                    break
                }
            }
            val resolvedId = if (found == -1) OkuyanIdCache.FALLBACK_ID else found
            OkuyanIdCache.id = resolvedId
            resolvedId
        } catch (_: Exception) {
            // SSL hatası veya ağ sorunu → bilinen sabit ID ile devam et
            OkuyanIdCache.id = OkuyanIdCache.FALLBACK_ID
            OkuyanIdCache.FALLBACK_ID
        }
    }

/**
 * quran.com API v4'ten Mehmet Okuyan Türkçe mealini sayfalı olarak getirir.
 * Büyük sureler (Bakara 286 ayet vb.) için sayfalama otomatik yapılır.
 */
private suspend fun fetchSurahContent(number: Int, meta: SurahMeta): SurahContent =
    withContext(Dispatchers.IO) {
        val translationId = resolveOkuyanId()
        val allVerses = mutableListOf<QuranVerse>()
        var page = 1
        do {
            val apiUrl = "https://api.quran.com/api/v4/verses/by_chapter/$number" +
                "?translations=$translationId&per_page=50&page=$page&fields=verse_number"
            val conn = (URL(apiUrl).openConnection() as HttpURLConnection).also {
                it.connectTimeout = 12_000
                it.readTimeout = 20_000
                it.setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) throw Exception("HTTP ${conn.responseCode}")
            val json = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            val root = JSONObject(json)
            val verses = root.getJSONArray("verses")
            for (i in 0 until verses.length()) {
                val v = verses.getJSONObject(i)
                val tArr = v.getJSONArray("translations")
                val text = if (tArr.length() > 0) stripHtml(tArr.getJSONObject(0).getString("text")) else ""
                allVerses.add(QuranVerse(
                    numberInSurah = v.getInt("verse_number"),
                    turkish = text
                ))
            }
            val totalPages = root.optJSONObject("pagination")?.optInt("total_pages", 1) ?: 1
            page++
        } while (page <= totalPages)
        SurahContent(meta, allVerses)
    }

// ─── Composable: Ana sekme ───────────────────────────────────────────────────

@Composable
fun MealTabContent() {
    var selectedSurah by rememberSaveable { mutableStateOf<Int?>(null) }

    if (selectedSurah == null) {
        SurahListView(onSelect = { selectedSurah = it })
    } else {
        val meta = ALL_SURAHS[selectedSurah!! - 1]
        SurahDetailView(
            surahNumber = selectedSurah!!,
            meta = meta,
            onBack = { selectedSurah = null }
        )
    }
}

// ─── Composable: Sure listesi ─────────────────────────────────────────────────

@Composable
private fun SurahListView(onSelect: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item { MealHeader() }

        items(ALL_SURAHS, key = { it.number }) { meta ->
            SurahListItem(meta = meta, onClick = { onSelect(meta.number) })
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun MealHeader() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                tint = Green700,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kur'an-ı Kerîm Meâli",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Prof. Dr. Mehmet Okuyan · 114 sure",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
            Text(
                text = "القرآن الكريم",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Green700
            )
        }
    }
}

@Composable
private fun SurahListItem(meta: SurahMeta, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sure numarası
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Green700, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = meta.number.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            // Türkçe ad + meta
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meta.nameTr,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${meta.verseCount} ayet · ${meta.revelation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Arapça ad
            Text(
                text = meta.nameAr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Green700
            )
        }
    }
}

// ─── Composable: Sure detayı ─────────────────────────────────────────────────

@Composable
private fun SurahDetailView(surahNumber: Int, meta: SurahMeta, onBack: () -> Unit) {
    BackHandler { onBack() }

    var content by remember { mutableStateOf<SurahContent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(surahNumber, retryKey) {
        isLoading = true
        errorMsg = null
        content = null
        try {
            content = fetchSurahContent(surahNumber, meta)
        } catch (e: Exception) {
            errorMsg = "İçerik yüklenemedi. İnternet bağlantınızı kontrol edin."
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Geri butonu + Sure başlığı
        SurahDetailHeader(meta = meta, onBack = onBack)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when {
            isLoading -> DetailLoadingState()
            errorMsg != null -> DetailErrorState(errorMsg!!) { retryKey++ }
            content != null -> SurahContentView(surahNumber = surahNumber, content = content!!)
        }
    }
}

@Composable
private fun SurahDetailHeader(meta: SurahMeta, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${meta.number}. ${meta.nameTr}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${meta.verseCount} ayet · ${meta.revelation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = meta.nameAr,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Green700,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@Composable
private fun DetailLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Green700)
            Text(
                text = "Sure yükleniyor…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tekrar dene")
            }
        }
    }
}

@Composable
private fun SurahContentView(surahNumber: Int, content: SurahContent) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        // Sesli okuma oynatıcısı
        item { QuranAudioPlayer(surahNumber) }

        // Besmele (Tevbe suresi hariç tüm sureler için)
        if (surahNumber != 9) {
            item { BismillahCard() }
        }

        // Ayetler
        items(
            content.verses,
            key = { "${surahNumber}_${it.numberInSurah}" },
            contentType = { "verse" }
        ) { verse ->
            VerseRow(verse)
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun BismillahCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            style = MaterialTheme.typography.titleLarge.copy(
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                lineHeight = 32.sp
            ),
            color = Green700
        )
    }
}

/**
 * Hafif ayet satırı — Card/gölge/kırpma yok, RTL Arapça metni yok.
 * Sadece ayet numarası rozeti + Mehmet Okuyan Türkçe meali gösterilir.
 */
@Composable
private fun VerseRow(verse: QuranVerse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Numara rozeti
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(Green700.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = verse.numberInSurah.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Green700
            )
        }
        // Türkçe meal (Mehmet Okuyan)
        Text(
            text = verse.turkish,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

// ─── Composable: Sesli okuma oynatıcısı ─────────────────────────────────────

@Composable
private fun QuranAudioPlayer(surahNumber: Int) {
    var playerState by remember { mutableStateOf(AudioPlayerState.IDLE) }
    val player = remember { MediaPlayer() }

    // Sure değiştiğinde veya composable kaldırıldığında oynatıcıyı serbest bırak
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (player.isPlaying) player.stop()
            } catch (_: Exception) { }
            player.release()
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Oynat / Duraklat / Yükleniyor butonu
            when (playerState) {
                AudioPlayerState.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp),
                        strokeWidth = 3.dp,
                        color = Green700
                    )
                }
                else -> {
                    IconButton(
                        onClick = {
                            when (playerState) {
                                AudioPlayerState.PLAYING -> {
                                    try { player.pause() } catch (_: Exception) { }
                                    playerState = AudioPlayerState.PAUSED
                                }
                                AudioPlayerState.PAUSED -> {
                                    try {
                                        player.start()
                                        playerState = AudioPlayerState.PLAYING
                                    } catch (_: Exception) {
                                        playerState = AudioPlayerState.ERROR
                                    }
                                }
                                else -> {
                                    // IDLE veya ERROR → yeniden başlat
                                    playerState = AudioPlayerState.LOADING
                                    try {
                                        player.reset()
                                        player.setAudioAttributes(
                                            AudioAttributes.Builder()
                                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                                .build()
                                        )
                                        player.setDataSource(surahAudioUrl(surahNumber))
                                        player.setOnPreparedListener { mp ->
                                            mp.start()
                                            playerState = AudioPlayerState.PLAYING
                                        }
                                        player.setOnErrorListener { _, _, _ ->
                                            playerState = AudioPlayerState.ERROR
                                            true
                                        }
                                        player.setOnCompletionListener {
                                            playerState = AudioPlayerState.IDLE
                                        }
                                        player.prepareAsync()
                                    } catch (_: Exception) {
                                        playerState = AudioPlayerState.ERROR
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (playerState == AudioPlayerState.PLAYING)
                                Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState == AudioPlayerState.PLAYING)
                                "Duraklat" else "Dinle",
                            tint = Green700,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Durum metni
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (playerState) {
                        AudioPlayerState.IDLE    -> "Sesli Kıraat Dinle"
                        AudioPlayerState.LOADING -> "Yükleniyor…"
                        AudioPlayerState.PLAYING -> "Çalıyor…"
                        AudioPlayerState.PAUSED  -> "Duraklatıldı"
                        AudioPlayerState.ERROR   -> "Ses yüklenemedi"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Kıraat: Mishary Alafasy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }

            Icon(
                Icons.Default.VolumeUp,
                contentDescription = null,
                tint = if (playerState == AudioPlayerState.PLAYING) Green700
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
