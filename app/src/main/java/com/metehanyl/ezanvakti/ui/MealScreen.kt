package com.metehanyl.ezanvakti.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
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
    val revelation: String
)

/** Tek bir Arapça kelime: metin, Latin okunuş ve Türkçe anlam */
data class QuranWord(
    val position: Int,
    val arabic: String,          // Arapça kelime
    val transliteration: String, // Latin harfli okunuş (bis'mi, allāhi…)
    val turkish: String          // Kelime düzeyinde Türkçe anlam
)

data class QuranVerse(
    val numberInSurah: Int,
    val arabic: String,           // text_uthmani — tam Arapça ayet metni
    val turkish: String,          // Mehmet Okuyan meali (ayet düzeyinde)
    val words: List<QuranWord>,   // Kelime kelime veri
    val transliteration: String = "" // Ayet düzeyinde Latin okunuş
)

data class SurahContent(
    val meta: SurahMeta,
    val verses: List<QuranVerse>
)

/** Görünüm modu */
enum class MealViewMode { MEAL, KELIME_KELIME }

/** Okuma işareti: yalnızca bir tane olabilir, yeni işaret eskiyi siler. */
data class MealBookmark(val surahNumber: Int, val verseNumber: Int)

/** Ayet arama sonucu */
data class VerseSearchResult(
    val surahNumber: Int,
    val verseNumber: Int,
    val surahNameTr: String,
    val arabic: String,
    val turkish: String
)

/** Sure listesi ekranı modu */
enum class SurahListMode { SURELER, AYET_ARA, AI_ARA }

// ─── Kelime verisi önbelleği (Sureler sekmesiyle paylaşılır) ──────────────────

internal object WordDataCache {
    val cache = mutableMapOf<Int, Map<Int, List<QuranWord>>>()
}

/**
 * alquran.cloud API'sinden ayet düzeyinde İngilizce transliterasyon getirir.
 * Dönen map: verseNumber → transliterasyon metni
 */
private suspend fun fetchTransliterations(surahNumber: Int): Map<Int, String> =
    withContext(Dispatchers.IO) {
        try {
            val url = "https://api.alquran.cloud/v1/surah/$surahNumber/en.transliteration"
            val conn = (URL(url).openConnection() as HttpURLConnection).also {
                it.connectTimeout = 10_000
                it.readTimeout = 15_000
                it.setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext emptyMap()
            val json = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            val ayahs = JSONObject(json).optJSONObject("data")?.optJSONArray("ayahs")
                ?: return@withContext emptyMap()
            val result = mutableMapOf<Int, String>()
            for (i in 0 until ayahs.length()) {
                val a = ayahs.getJSONObject(i)
                val num = a.optInt("numberInSurah", -1)
                val text = a.optString("text", "")
                if (num > 0 && text.isNotBlank()) result[num] = text
            }
            result
        } catch (_: Exception) { emptyMap() }
    }

/**
 * quran.com arama API'sini kullanarak ayet arar.
 * Her iki mod (Ayet Ara + AI Ara) aynı endpoint'i kullanır.
 */
private suspend fun searchVerses(query: String, size: Int = 20): List<VerseSearchResult> =
    withContext(Dispatchers.IO) {
        try {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://api.quran.com/api/v4/search?q=$encoded&size=$size&page=1&language=tr"
            val conn = (URL(url).openConnection() as HttpURLConnection).also {
                it.connectTimeout = 12_000
                it.readTimeout = 20_000
                it.setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext emptyList()
            val json = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            val results = JSONObject(json)
                .optJSONObject("search")
                ?.optJSONArray("results")
                ?: return@withContext emptyList()
            val list = mutableListOf<VerseSearchResult>()
            for (i in 0 until results.length()) {
                val r = results.getJSONObject(i)
                val key = r.optString("verse_key", "")
                val parts = key.split(":")
                if (parts.size != 2) continue
                val surahNum = parts[0].toIntOrNull() ?: continue
                val verseNum = parts[1].toIntOrNull() ?: continue
                val surahMeta = ALL_SURAHS.getOrNull(surahNum - 1) ?: continue
                val arabicText = r.optString("text", "")
                val translationsArr = r.optJSONArray("translations")
                val turkishText = if (translationsArr != null && translationsArr.length() > 0)
                    stripHtml(translationsArr.getJSONObject(0).optString("text", ""))
                else ""
                list.add(VerseSearchResult(surahNum, verseNum, surahMeta.nameTr, arabicText, turkishText))
            }
            list
        } catch (_: Exception) { emptyList() }
    }

/**
 * quran.com API v4'ten belirtilen sure için kelime düzeyinde
 * transliterasyon ve Türkçe anlam verisini getirir.
 * Sonuç WordDataCache'e kaydedilir; aynı sure tekrar istenmez.
 */
suspend fun fetchWordData(surahNumber: Int): Map<Int, List<QuranWord>> =
    withContext(Dispatchers.IO) {
        WordDataCache.cache[surahNumber]?.let { return@withContext it }
        val result = mutableMapOf<Int, MutableList<QuranWord>>()
        var page = 1
        var totalPages = 1
        do {
            val url = "https://api.quran.com/api/v4/verses/by_chapter/$surahNumber" +
                "?word_fields=transliteration,translation" +
                "&per_page=50&page=$page" +
                "&fields=verse_number"
            val conn = (URL(url).openConnection() as HttpURLConnection).also {
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
                val verseNum = v.getInt("verse_number")
                val wordsArr = v.optJSONArray("words") ?: continue
                val list = result.getOrPut(verseNum) { mutableListOf() }
                for (j in 0 until wordsArr.length()) {
                    val w = wordsArr.getJSONObject(j)
                    if (w.optString("char_type_name") == "end") continue
                    val wordAr = w.optString("text_uthmani", w.optString("text", ""))
                    val translit = w.optJSONObject("transliteration")?.optString("text", "") ?: ""
                    val wordTr = w.optJSONObject("translation")?.optString("text", "") ?: ""
                    if (wordAr.isNotBlank()) list.add(QuranWord(w.optInt("position", j + 1), wordAr, translit, wordTr))
                }
            }
            totalPages = root.optJSONObject("pagination")?.optInt("total_pages", 1) ?: 1
            page++
        } while (page <= totalPages)
        val immutable: Map<Int, List<QuranWord>> = result
        WordDataCache.cache[surahNumber] = immutable
        immutable
    }

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
    SurahMeta(64,  "Teğâbün",      "التغabün",   18,   "Medenî"),
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
    val verse  = prefs.getInt(KEY_BM_VERSE,  -1)
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

private fun stripHtml(text: String): String =
    text.replace(Regex("<[^>]+>"), "").trim()

private object OkuyanIdCache {
    @Volatile var id: Int? = null
    const val FALLBACK_ID = 112
}

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
                if (combined.contains("Okuyan", ignoreCase = true)) { found = t.getInt("id"); break }
            }
            val resolved = if (found == -1) OkuyanIdCache.FALLBACK_ID else found
            OkuyanIdCache.id = resolved; resolved
        } catch (_: Exception) {
            OkuyanIdCache.id = OkuyanIdCache.FALLBACK_ID; OkuyanIdCache.FALLBACK_ID
        }
    }

private suspend fun fetchSurahContent(number: Int, meta: SurahMeta): SurahContent =
    withContext(Dispatchers.IO) {
        val translationId = resolveOkuyanId()
        // alquran.cloud'dan ayet düzeyinde okunuş verisini önceden çek
        val transliterationMap = fetchTransliterations(number)
        val allVerses = mutableListOf<QuranVerse>()
        var page = 1
        do {
            val apiUrl = "https://api.quran.com/api/v4/verses/by_chapter/$number" +
                "?translations=$translationId" +
                "&word_fields=transliteration,translation" +
                "&per_page=50&page=$page" +
                "&fields=text_uthmani,verse_number"
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
                val arabicText = v.optString("text_uthmani", "")
                // Ayet düzeyinde okunuş: alquran.cloud haritasından al;
                // yoksa quran.com transliterations dizisi/objesi dene
                val verseNum0 = v.optInt("verse_number", -1)
                val translitText = transliterationMap[verseNum0] ?: run {
                    val arr = v.optJSONArray("transliterations")
                    if (arr != null && arr.length() > 0)
                        arr.getJSONObject(0).optString("text", "")
                    else
                        v.optJSONObject("transliteration")?.optString("text", "") ?: ""
                }
                val tArr = v.getJSONArray("translations")
                val turkishText = if (tArr.length() > 0) stripHtml(tArr.getJSONObject(0).getString("text")) else ""
                // Kelime verisi
                val wordsArr = v.optJSONArray("words")
                val words = mutableListOf<QuranWord>()
                if (wordsArr != null) {
                    for (j in 0 until wordsArr.length()) {
                        val w = wordsArr.getJSONObject(j)
                        if (w.optString("char_type_name") == "end") continue
                        val wAr     = w.optString("text_uthmani", w.optString("text", ""))
                        val translit = w.optJSONObject("transliteration")?.optString("text", "") ?: ""
                        val wTr     = w.optJSONObject("translation")?.optString("text", "") ?: ""
                        if (wAr.isNotBlank()) words.add(QuranWord(w.optInt("position", j + 1), wAr, translit, wTr))
                    }
                }
                // Kelime önbelleğini de güncelle (Sureler sekmesiyle paylaşım)
                if (words.isNotEmpty()) {
                    val cached = WordDataCache.cache.getOrPut(number) { mutableMapOf() }.toMutableMap()
                    cached[verseNum0] = words
                    WordDataCache.cache[number] = cached
                }
                allVerses.add(QuranVerse(
                    numberInSurah = verseNum0,
                    arabic = arabicText,
                    turkish = turkishText,
                    words = words,
                    transliteration = translitText
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
    var bookmark by remember { mutableStateOf(loadBookmark(context)) }

    val onBookmark: (MealBookmark?) -> Unit = { nb -> bookmark = nb; saveBookmark(context, nb) }

    if (selectedSurah == null) {
        SurahListView(bookmark = bookmark, onSelect = { selectedSurah = it })
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
    var listMode by rememberSaveable { mutableStateOf(SurahListMode.SURELER) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Mod seçici chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = listMode == SurahListMode.SURELER,
                onClick = { listMode = SurahListMode.SURELER },
                label = { Text("📖 Sureler", style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Green700,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = listMode == SurahListMode.AYET_ARA,
                onClick = { listMode = SurahListMode.AYET_ARA },
                label = { Text("🔍 Ayet Ara", style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Green700,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = listMode == SurahListMode.AI_ARA,
                onClick = { listMode = SurahListMode.AI_ARA },
                label = { Text("✨ AI Ara", style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Green700,
                    selectedLabelColor = Color.White
                )
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        when (listMode) {
            SurahListMode.SURELER -> SurahBrowserContent(bookmark = bookmark, onSelect = onSelect)
            SurahListMode.AYET_ARA -> VerseSearchContent(onSelect = onSelect, isAiMode = false)
            SurahListMode.AI_ARA -> VerseSearchContent(onSelect = onSelect, isAiMode = true)
        }
    }
}

@Composable
private fun SurahBrowserContent(bookmark: MealBookmark?, onSelect: (Int) -> Unit) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) ALL_SURAHS
        else ALL_SURAHS.filter { meta ->
            meta.nameTr.contains(searchQuery, ignoreCase = true) ||
            meta.nameAr.contains(searchQuery) ||
            meta.number.toString() == searchQuery.trim()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item { MealHeader(bookmark = bookmark) }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                placeholder = {
                    Text("Sure ara (ad veya numara)…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                leadingIcon = { Icon(Icons.Default.Search, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }
        if (filtered.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Sonuç bulunamadı", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        items(filtered, key = { it.number }) { meta ->
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
private fun VerseSearchContent(onSelect: (Int) -> Unit, isAiMode: Boolean) {
    var query by rememberSaveable { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<VerseSearchResult>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }
    var expandedVerse by remember { mutableStateOf<Pair<Int,Int>?>(null) }
    val scope = rememberCoroutineScope()

    fun doSearch() {
        if (query.isBlank() || isSearching) return
        scope.launch {
            isSearching = true
            hasSearched = true
            results = searchVerses(query)
            isSearching = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // AI modu: açıklama kartı
        if (isAiMode) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Green700.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text("Konu ile Ayet Ara",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Green700)
                            Text("Bir konu veya cümle yaz, Kur'an'dan ilgili ayetler listelensin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Arama kutusu + buton
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (isAiMode) "Örnek: sabrın önemi, şükür, merhamet…"
                            else "Kelime veya cümle ara…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(if (isAiMode) Icons.Default.Search else Icons.Default.Search,
                            null, tint = if (isAiMode) Green700 else MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = ""; results = emptyList(); hasSearched = false }) {
                                Icon(Icons.Default.Clear, "Temizle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    minLines = if (isAiMode) 2 else 1,
                    maxLines = if (isAiMode) 4 else 1,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = if (isAiMode) ImeAction.Default else ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { doSearch() })
                )
                Button(
                    onClick = { doSearch() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = query.isNotBlank() && !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isAiMode) "Analiz ediliyor…" else "Aranıyor…")
                    } else {
                        Text(if (isAiMode) "✨ AI ile Ara" else "🔍 Ayetlerde Ara")
                    }
                }
            }
        }

        // Sonuç başlığı
        if (hasSearched && !isSearching) {
            item {
                Text(
                    text = if (results.isEmpty()) "Sonuç bulunamadı"
                           else if (isAiMode) "✨ ${results.size} ilgili ayet bulundu"
                           else "🔍 ${results.size} ayet bulundu",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (results.isEmpty()) MaterialTheme.colorScheme.error else Green700,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Sonuç kartları
        items(results, key = { "${it.surahNumber}_${it.verseNumber}" }) { result ->
            val isExpanded = expandedVerse == (result.surahNumber to result.verseNumber)
            VerseSearchResultCard(
                result = result,
                isExpanded = isExpanded,
                onToggleExpand = {
                    expandedVerse = if (isExpanded) null else (result.surahNumber to result.verseNumber)
                },
                onGoToSurah = { onSelect(result.surahNumber) }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun VerseSearchResultCard(
    result: VerseSearchResult,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onGoToSurah: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Başlık satırı: Sure adı + ayet rozeti + "Sureye Git →"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Green700, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${result.surahNumber}:${result.verseNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = result.surahNameTr,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Sureye Git →",
                    style = MaterialTheme.typography.labelSmall,
                    color = Green700,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onGoToSurah)
                )
            }

            // Arapça aç/kapat
            if (result.arabic.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExpand)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Green700.copy(alpha = 0.65f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isExpanded) "Gizle" else "Arapça · Okunuş",
                        style = MaterialTheme.typography.labelSmall,
                        color = Green700.copy(alpha = 0.65f)
                    )
                }
                if (isExpanded) {
                    Text(
                        text = result.arabic,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDirection = TextDirection.Rtl,
                            textAlign = TextAlign.End,
                            fontSize = 18.sp,
                            lineHeight = 32.sp
                        ),
                        color = Green700.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Türkçe meal (her zaman görünür)
            if (result.turkish.isNotBlank()) {
                Text(
                    text = result.turkish,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MealHeader(bookmark: MealBookmark?) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.MenuBook, null, tint = Green700, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Kur'an-ı Kerîm Meâli", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    text = if (bookmark != null) {
                        "📖 ${ALL_SURAHS.getOrNull(bookmark.surahNumber - 1)?.nameTr ?: ""} · ${bookmark.verseNumber}. ayet"
                    } else "Prof. Dr. Mehmet Okuyan · 114 sure",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bookmark != null) Green700 else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = if (bookmark != null) FontStyle.Normal else FontStyle.Italic
                )
            }
            Text("القرآن الكريم", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = Green700)
        }
    }
}

@Composable
private fun SurahListItem(meta: SurahMeta, isBookmarked: Boolean, bookmarkedVerse: Int?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBookmarked) MaterialTheme.colorScheme.secondaryContainer
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(38.dp).background(Green700, CircleShape), contentAlignment = Alignment.Center) {
                Text(meta.number.toString(), color = Color.White,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(meta.nameTr, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBookmarked) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurface)
                Text(
                    text = if (isBookmarked && bookmarkedVerse != null)
                        "${meta.verseCount} ayet · ${meta.revelation}  ·  📖 ${bookmarkedVerse}. ayetten devam"
                    else "${meta.verseCount} ayet · ${meta.revelation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isBookmarked) Green700 else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isBookmarked) {
                Icon(Icons.Default.Bookmark, "İşaretlendi", tint = Green700, modifier = Modifier.size(22.dp))
            } else {
                Text(meta.nameAr, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Green700)
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

    var content   by remember { mutableStateOf<SurahContent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg  by remember { mutableStateOf<String?>(null) }
    var retryKey  by remember { mutableIntStateOf(0) }
    var viewMode  by rememberSaveable { mutableStateOf(MealViewMode.MEAL) }

    LaunchedEffect(surahNumber, retryKey) {
        isLoading = true; errorMsg = null; content = null
        try { content = fetchSurahContent(surahNumber, meta) }
        catch (e: Exception) { errorMsg = "İçerik yüklenemedi. İnternet bağlantınızı kontrol edin." }
        finally { isLoading = false }
    }

    // Transliterasyon için kelime verisi önbellekte yoksa ayrıca yükle
    LaunchedEffect(surahNumber) {
        if (WordDataCache.cache[surahNumber] == null) {
            try { fetchWordData(surahNumber) } catch (_: Exception) { }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SurahDetailHeader(meta = meta, viewMode = viewMode, onViewModeChange = { viewMode = it }, onBack = onBack)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        when {
            isLoading  -> DetailLoadingState()
            errorMsg != null -> DetailErrorState(errorMsg!!) { retryKey++ }
            content != null  -> SurahContentView(
                surahNumber = surahNumber,
                content = content!!,
                viewMode = viewMode,
                bookmark = bookmark,
                onBookmark = onBookmark
            )
        }
    }
}

@Composable
private fun SurahDetailHeader(
    meta: SurahMeta,
    viewMode: MealViewMode,
    onViewModeChange: (MealViewMode) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") }
            Column(modifier = Modifier.weight(1f)) {
                Text("${meta.number}. ${meta.nameTr}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${meta.verseCount} ayet · ${meta.revelation}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(meta.nameAr, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = Green700, modifier = Modifier.padding(end = 12.dp))
        }
        // Mod seçici
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = viewMode == MealViewMode.MEAL,
                onClick = { onViewModeChange(MealViewMode.MEAL) },
                label = { Text("Meâl", style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Green700,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = viewMode == MealViewMode.KELIME_KELIME,
                onClick = { onViewModeChange(MealViewMode.KELIME_KELIME) },
                label = { Text("Kelime Kelime", style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Green700,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun DetailLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = Green700)
            Text("Sure yükleniyor…", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tekrar dene")
            }
        }
    }
}

@Composable
private fun SurahContentView(
    surahNumber: Int,
    content: SurahContent,
    viewMode: MealViewMode,
    bookmark: MealBookmark?,
    onBookmark: (MealBookmark?) -> Unit
) {
    val bookmarkedIndex = if (bookmark?.surahNumber == surahNumber) {
        val besmeleOffset = if (surahNumber != 9) 1 else 0
        val verseIndex = content.verses.indexOfFirst { it.numberInSurah == bookmark.verseNumber }
        if (verseIndex >= 0) 1 + besmeleOffset + verseIndex else -1
    } else -1

    val listState = rememberLazyListState()
    LaunchedEffect(bookmarkedIndex) { if (bookmarkedIndex > 0) listState.scrollToItem(bookmarkedIndex) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        if (surahNumber != 9) { item { BismillahCard() } }

        items(content.verses, key = { "${surahNumber}_${it.numberInSurah}" }, contentType = { "verse" }) { verse ->
            val isBookmarked = bookmark?.surahNumber == surahNumber && bookmark.verseNumber == verse.numberInSurah
            VerseRow(
                verse = verse,
                surahNumber = surahNumber,
                viewMode = viewMode,
                isBookmarked = isBookmarked,
                onToggleBookmark = {
                    onBookmark(if (isBookmarked) null else MealBookmark(surahNumber, verse.numberInSurah))
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 16.dp),
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

// ─── Ayet satırı (her iki mod) ────────────────────────────────────────────────

@Composable
private fun VerseRow(
    verse: QuranVerse,
    surahNumber: Int,
    viewMode: MealViewMode,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
) {
    val bgColor = if (isBookmarked) Green700.copy(alpha = 0.08f) else Color.Transparent
    // Meâl modunda Arapça + okunuş varsayılan olarak gizli; kullanıcı açar
    var arabicExpanded by rememberSaveable(surahNumber, verse.numberInSurah) { mutableStateOf(false) }

    // SurahVerseBlock ile aynı Row düzeni: [rozet] [içerik] [işaret]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Ayet numarası rozeti
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(if (isBookmarked) Green700 else Green700.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(verse.numberInSurah.toString(), style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, color = if (isBookmarked) Color.White else Green700)
        }

        // İçerik
        Column(modifier = Modifier.weight(1f)) {
            when (viewMode) {
                MealViewMode.MEAL -> MealModeContent(
                    verse = verse,
                    surahNumber = surahNumber,
                    arabicExpanded = arabicExpanded,
                    onToggleArabic = { arabicExpanded = !arabicExpanded }
                )
                MealViewMode.KELIME_KELIME -> KelimeKelimeModeContent(verse, surahNumber)
            }
        }

        // Yer işareti butonu
        IconButton(onClick = onToggleBookmark, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (isBookmarked) "İşareti kaldır" else "İşaret koy",
                tint = if (isBookmarked) Green700 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (!isBookmarked) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 12.dp))
    }
}

/**
 * MEAL modu — varsayılanda yalnızca Türkçe meal görünür.
 * Üstteki satıra dokunarak Arapça metin + okunuş açılır/kapanır.
 */
@Composable
private fun MealModeContent(
    verse: QuranVerse,
    surahNumber: Int,
    arabicExpanded: Boolean,
    onToggleArabic: () -> Unit
) {
    // Arapça / okunuş aç-kapat satırı (her zaman görünür)
    val hasArabic = verse.arabic.isNotBlank()
    val translit = verse.transliteration.ifBlank {
        val words = verse.words.ifEmpty {
            WordDataCache.cache[surahNumber]?.get(verse.numberInSurah) ?: emptyList()
        }
        words.joinToString(" ") { it.transliteration }.trim()
    }
    if (hasArabic || translit.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleArabic)
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (arabicExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (arabicExpanded) "Gizle" else "Arapça ve okunuşu göster",
                tint = Green700.copy(alpha = 0.65f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (arabicExpanded) "Gizle" else "Arapça · Okunuş",
                style = MaterialTheme.typography.labelSmall,
                color = Green700.copy(alpha = 0.65f)
            )
        }
    }

    // 1. Arapça metin + 2. Okunuş — yalnızca açıkken görünür
    if (arabicExpanded) {
        if (hasArabic) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = verse.arabic,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDirection = TextDirection.Rtl,
                    textAlign = TextAlign.End,
                    fontSize = 19.sp,
                    lineHeight = 34.sp
                ),
                color = Green700.copy(alpha = 0.9f)
            )
        }
        if (translit.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = translit,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Green700.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                fontStyle = FontStyle.Italic
            )
        }
        Spacer(Modifier.height(6.dp))
    }

    // 3. Türkçe meal — her zaman görünür
    if (verse.turkish.isNotBlank()) {
        Text(
            text = verse.turkish,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** KELIME KELIME modu: her Arapça kelimenin altında okunuşu ve Türkçe anlamı */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KelimeKelimeModeContent(verse: QuranVerse, surahNumber: Int) {
    if (verse.words.isEmpty()) {
        // Kelime verisi yoksa normal moda düş — Arapça varsayılan açık
        MealModeContent(verse, surahNumber, arabicExpanded = true, onToggleArabic = {})
        return
    }
    // RTL düzende kelime sütunları — her kelime: Arapça / okunuş / anlam
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            verse.words.forEach { word ->
                WordColumn(word)
            }
        }
    }
    Spacer(Modifier.height(6.dp))
    // Altında tam Türkçe meal (referans için)
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        modifier = Modifier.padding(vertical = 4.dp))
    Text(
        text = verse.turkish,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 18.sp
    )
}

/** Tek kelime sütunu: Arapça üstte, okunuş ortada, Türkçe anlam altta */
@Composable
internal fun WordColumn(word: QuranWord) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arapça kelime (RTL miras alır — doğru görünür)
        Text(
            text = word.arabic,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold,
            color = Green700
        )
        // Okunuş ve anlam — LTR olmalı
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            if (word.transliteration.isNotBlank()) {
                Text(
                    text = word.transliteration,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
            if (word.turkish.isNotBlank()) {
                Text(
                    text = word.turkish,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
