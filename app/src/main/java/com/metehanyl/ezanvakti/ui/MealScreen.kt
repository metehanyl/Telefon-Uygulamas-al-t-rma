package com.metehanyl.ezanvakti.ui

import android.content.Context
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
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

/** Okuma işareti: yalnızca bir tane olabilir, yeni işaret eskiyi siler. */
data class MealBookmark(val surahNumber: Int, val verseNumber: Int)

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

// ─── Okuma işareti (SharedPreferences) ───────────────────────────────────────

private const val PREFS_NAME = "meal_prefs"
private const val KEY_BM_SURAH = "bookmark_surah"
private const val KEY_BM_VERSE = "bookmark_verse"

private fun loadBookmark(context: Context): MealBookmark? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val surah = prefs.getInt(KEY_BM_SURAH, -1)
    val verse = prefs.getInt(KEY_BM_VERSE, -1)
    return if (surah != -1 && verse != -1) MealBookmark(surah, verse) else null
}

private fun saveBookmark(context: Context, bookmark: MealBookmark?) {
    val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
    if (bookmark == null) {
        editor.remove(KEY_BM_SURAH).remove(KEY_BM_VERSE)
    } else {
        editor.putInt(KEY_BM_SURAH, bookmark.surahNumber)
              .putInt(KEY_BM_VERSE, bookmark.verseNumber)
    }
    editor.apply()
}

// ─── Yardımcı fonksiyonlar ───────────────────────────────────────────────────

/** quran.com API'nin döndürdüğü <sup>…</sup> dipnot etiketlerini temizler */
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
    val context = LocalContext.current
    var selectedSurah by rememberSaveable { mutableStateOf<Int?>(null) }
    // Bookmark: uygulama başlangıcında SharedPreferences'tan yüklenir
    var bookmark by remember { mutableStateOf(loadBookmark(context)) }

    val onBookmark: (MealBookmark?) -> Unit = { newBookmark ->
        bookmark = newBookmark
        saveBookmark(context, newBookmark)
    }

    if (selectedSurah == null) {
        SurahListView(
            bookmark = bookmark,
            onSelect = { selectedSurah = it }
        )
    } else {
        val meta = ALL_SURAHS[selectedSurah!! - 1]
        SurahDetailView(
            surahNumber = selectedSurah!!,
            meta = meta,
            bookmark = bookmark,
            onBookmark = onBookmark,
            onBack = { selectedSurah = null }
        )
    }
}

// ─── Composable: Sure listesi ─────────────────────────────────────────────────

@Composable
private fun SurahListView(bookmark: MealBookmark?, onSelect: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item { MealHeader(bookmark = bookmark) }

        items(ALL_SURAHS, key = { it.number }) { meta ->
            SurahListItem(
                meta = meta,
                isBookmarked = bookmark?.surahNumber == meta.number,
                bookmarkedVerse = if (bookmark?.surahNumber == meta.number) bookmark?.verseNumber else null,
                onClick = { onSelect(meta.number) }
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun MealHeader(bookmark: MealBookmark?) {
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
                    text = if (bookmark != null) {
                        val sureName = ALL_SURAHS.getOrNull(bookmark.surahNumber - 1)?.nameTr ?: ""
                        "📖 ${sureName} · ${bookmark.verseNumber}. ayet"
                    } else {
                        "Prof. Dr. Mehmet Okuyan · 114 sure"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bookmark != null) Green700
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = if (bookmark != null) FontStyle.Normal else FontStyle.Italic
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
private fun SurahListItem(
    meta: SurahMeta,
    isBookmarked: Boolean,
    bookmarkedVerse: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBookmarked)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
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
                    .background(
                        if (isBookmarked) Green700 else Green700,
                        CircleShape
                    ),
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
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBookmarked)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBookmarked && bookmarkedVerse != null)
                        "${meta.verseCount} ayet · ${meta.revelation}  ·  📖 ${bookmarkedVerse}. ayetten devam"
                    else
                        "${meta.verseCount} ayet · ${meta.revelation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isBookmarked) Green700
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // İşaret ikonu veya Arapça ad
            if (isBookmarked) {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = "İşaretlendi",
                    tint = Green700,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = meta.nameAr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green700
                )
            }
        }
    }
}

// ─── Composable: Sure detayı ─────────────────────────────────────────────────

@Composable
private fun SurahDetailView(
    surahNumber: Int,
    meta: SurahMeta,
    bookmark: MealBookmark?,
    onBookmark: (MealBookmark?) -> Unit,
    onBack: () -> Unit
) {
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
        SurahDetailHeader(meta = meta, onBack = onBack)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when {
            isLoading -> DetailLoadingState()
            errorMsg != null -> DetailErrorState(errorMsg!!) { retryKey++ }
            content != null -> SurahContentView(
                surahNumber = surahNumber,
                content = content!!,
                bookmark = bookmark,
                onBookmark = onBookmark
            )
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
private fun SurahContentView(
    surahNumber: Int,
    content: SurahContent,
    bookmark: MealBookmark?,
    onBookmark: (MealBookmark?) -> Unit
) {
    // İşaretli ayet varsa o ayetin index'ine kaydır
    val bookmarkedIndex = if (bookmark?.surahNumber == surahNumber) {
        // Besmele (+1 offset) ve başlık item'larını say
        val besmeleOffset = if (surahNumber != 9) 1 else 0  // besmele item
        val headerOffset = 1  // spacer item
        val verseIndex = content.verses.indexOfFirst { it.numberInSurah == bookmark.verseNumber }
        if (verseIndex >= 0) headerOffset + besmeleOffset + verseIndex else -1
    } else -1

    val listState = rememberLazyListState()

    // Sayfa açılınca işaretli ayete scroll
    LaunchedEffect(bookmarkedIndex) {
        if (bookmarkedIndex > 0) {
            listState.scrollToItem(bookmarkedIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }

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
            val isThisBookmarked = bookmark?.surahNumber == surahNumber &&
                                   bookmark.verseNumber == verse.numberInSurah
            VerseRow(
                verse = verse,
                isBookmarked = isThisBookmarked,
                onToggleBookmark = {
                    if (isThisBookmarked) {
                        // Aynı ayete tekrar basınca işareti kaldır
                        onBookmark(null)
                    } else {
                        // Yeni işaret: eskisi otomatik kalkar
                        onBookmark(MealBookmark(surahNumber, verse.numberInSurah))
                    }
                }
            )
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
 * Ayet satırı — ayet numarası rozeti + Türkçe meal + işaret butonu.
 * İşaretli ayet yeşil arka planla vurgulanır.
 * İşaret butonuna basınca: işaretli değilse işaret koy, işaretliyse kaldır.
 * Yeni işaret konunca önceki işaret otomatik kalkar (üst bileşen yönetir).
 */
@Composable
private fun VerseRow(
    verse: QuranVerse,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
) {
    val bgColor = if (isBookmarked)
        Green700.copy(alpha = 0.08f)
    else
        Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Numara rozeti
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    if (isBookmarked) Green700 else Green700.copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = verse.numberInSurah.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isBookmarked) Color.White else Green700
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
        // İşaret butonu
        IconButton(
            onClick = onToggleBookmark,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (isBookmarked) "İşareti kaldır" else "Buraya işaret koy",
                tint = if (isBookmarked) Green700 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
    if (!isBookmarked) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
