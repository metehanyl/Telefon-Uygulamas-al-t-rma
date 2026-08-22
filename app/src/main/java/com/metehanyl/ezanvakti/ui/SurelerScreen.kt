package com.metehanyl.ezanvakti.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metehanyl.ezanvakti.ui.theme.Green700

// ---------------------------------------------------------------------------
// Veri modeli
// ---------------------------------------------------------------------------

/** Tek bir ayet: Arapça metin, Türkçe fonetik okunuş ve Türkçe anlam */
data class SurahVerseData(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val turkish: String
)

data class Surah(
    val number: Int,
    val nameTr: String,
    val nameAr: String,
    val verseCount: Int,
    val verses: List<SurahVerseData>   // Her sure ayet bazında tutulur
)

// ---------------------------------------------------------------------------
// Namaz Sureleri – Prof. Dr. Mehmet Okuyan Meali (ayet bazında)
// ---------------------------------------------------------------------------

val NAMAZ_SURELERI: List<Surah> = listOf(

    // ── Fâtiha (1) ──────────────────────────────────────────────────────────
    // Not: Fâtiha'da besmele 1. ayettir
    Surah(
        number = 1, nameTr = "Fâtiha", nameAr = "الفاتحة", verseCount = 7,
        verses = listOf(
            SurahVerseData(1,
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                "Bismillahirrahmanirrahim",
                "Rahman ve Rahim olan Allah'ın adıyla."),
            SurahVerseData(2,
                "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                "Elhamdü lillahi rabbil alemin",
                "Hamd, âlemlerin Rabbi Allah'a aittir."),
            SurahVerseData(3,
                "الرَّحْمَٰنِ الرَّحِيمِ",
                "Errahmanirrahim",
                "O Rahman'dır, Rahim'dir."),
            SurahVerseData(4,
                "مَالِكِ يَوْمِ الدِّينِ",
                "Maliki yevmiddin",
                "Din gününün sahibidir."),
            SurahVerseData(5,
                "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                "İyyake na'büdü ve iyyake nesteın",
                "Yalnız sana kulluk eder, yalnız senden yardım dileriz."),
            SurahVerseData(6,
                "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                "İhdinessıratelmüstekim",
                "Bizi dosdoğru yola ilet."),
            SurahVerseData(7,
                "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                "Sıratelleziyne en'amte aleyhim, gayril mağdubi aleyhim ve leddallîn",
                "Nimet verdiklerinin yoluna; gazaba uğrayanların ve sapkınların yoluna değil.")
        )
    ),

    // ── Asr (103) ───────────────────────────────────────────────────────────
    Surah(
        number = 103, nameTr = "Asr", nameAr = "العصر", verseCount = 3,
        verses = listOf(
            SurahVerseData(1,
                "وَالْعَصْرِ",
                "Vel asri",
                "Asra/zamana andolsun,"),
            SurahVerseData(2,
                "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ",
                "İnnel insane lefiy hüsrin",
                "Şüphesiz insan gerçekten hüsran içindedir."),
            SurahVerseData(3,
                "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ",
                "İllellezine amenu veamilüssalihati ve tevasav bilhakki ve tevasav bissabr",
                "Ancak iman edip salih amel işleyenler, birbirlerine hakkı tavsiye edenler ve birbirlerine sabrı tavsiye edenler bunun dışındadır.")
        )
    ),

    // ── Fîl (105) ───────────────────────────────────────────────────────────
    Surah(
        number = 105, nameTr = "Fîl", nameAr = "الفيل", verseCount = 5,
        verses = listOf(
            SurahVerseData(1,
                "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ",
                "Elem tera keyfe feale rabbüke bi ashabil fil",
                "Rabbin, fil sahiplerine ne yaptı; görmedin mi?"),
            SurahVerseData(2,
                "أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ",
                "Elem yec'al keydehüm fi tadlil",
                "Onların düzenlerini/tuzaklarını boşa çıkarmadı mı?"),
            SurahVerseData(3,
                "وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ",
                "Ve ersele aleyhim tayran ebebile",
                "Üzerlerine sürüler hâlinde Ebabil kuşları gönderdi."),
            SurahVerseData(4,
                "تَرْمِيهِم بِحِجَارَةٍ مِّن سِجِّيلٍ",
                "Termihim bihicaratin min siccil",
                "Onlara pişirilmiş çamurdan taşlar attırdı."),
            SurahVerseData(5,
                "فَجَعَلَهُمْ كَعَصْفٍ مَّأْكُولٍ",
                "Fece'alehüm ke'asfin me'kul",
                "Böylece onları yenilmiş ekin sapı gibi ezip geçirdi.")
        )
    ),

    // ── Kureyş (106) ────────────────────────────────────────────────────────
    Surah(
        number = 106, nameTr = "Kureyş", nameAr = "قريش", verseCount = 4,
        verses = listOf(
            SurahVerseData(1,
                "لِإِيلَافِ قُرَيْشٍ",
                "Li'ilafi Kureyşin",
                "Kureyş'in yolculuklarına/ticaretlerine alıştırıldığı için,"),
            SurahVerseData(2,
                "إِيلَافِهِمْ رِحْلَةَ الشِّتَاءِ وَالصَّيْفِ",
                "İylafihim rihletesşita'i vessayfi",
                "Onların kış ve yaz yolculuklarına alıştırıldıkları için."),
            SurahVerseData(3,
                "فَلْيَعْبُدُوا رَبَّ هَٰذَا الْبَيْتِ",
                "Felyabudu rabbe hazelbeyti",
                "O hâlde bu evin Rabbine kulluk etsinler."),
            SurahVerseData(4,
                "الَّذِي أَطْعَمَهُم مِّن جُوعٍ وَآمَنَهُم مِّنْ خَوْفٍ",
                "Ellezi et'amehüm min cu'in ve amenehüm min havf",
                "O ki onları açlıktan doyurmuş ve korkudan güvene kavuşturmuştur.")
        )
    ),

    // ── Mâun (107) ──────────────────────────────────────────────────────────
    Surah(
        number = 107, nameTr = "Mâun", nameAr = "الماعون", verseCount = 7,
        verses = listOf(
            SurahVerseData(1,
                "أَرَأَيْتَ الَّذِي يُكَذِّبُ بِالدِّينِ",
                "Eraeytellezi yükezzibü biddini",
                "Dini/hesap gününü yalanlayanı gördün mü?"),
            SurahVerseData(2,
                "فَذَٰلِكَ الَّذِي يَدُعُّ الْيَتِيمَ",
                "Fezalikellezi yedüül yetime",
                "İşte o, yetimi şiddetle iter."),
            SurahVerseData(3,
                "وَلَا يَحُضُّ عَلَىٰ طَعَامِ الْمِسْكِينِ",
                "Ve la yehuddu ala taamil miskini",
                "Yoksulun doyurulmasına teşvik etmez."),
            SurahVerseData(4,
                "فَوَيْلٌ لِّلْمُصَلِّينَ",
                "Fevevylün lilmüsalline",
                "Vay hâline o namaz kılanlara ki,"),
            SurahVerseData(5,
                "الَّذِينَ هُمْ عَن صَلَاتِهِمْ سَاهُونَ",
                "Ellezine hüm an salatihim sahune",
                "Onlar namazlarından gafildirler."),
            SurahVerseData(6,
                "الَّذِينَ هُمْ يُرَاءُونَ",
                "Ellezine hüm yüraune",
                "Onlar gösteriş için yaparlar."),
            SurahVerseData(7,
                "وَيَمْنَعُونَ الْمَاعُونَ",
                "Ve yemne'unel maune",
                "Yardımı/zekâtı da engellerler.")
        )
    ),

    // ── Kevser (108) ────────────────────────────────────────────────────────
    Surah(
        number = 108, nameTr = "Kevser", nameAr = "الكوثر", verseCount = 3,
        verses = listOf(
            SurahVerseData(1,
                "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ",
                "İnna a'taynakel kevser",
                "Şüphesiz biz sana Kevser'i verdik."),
            SurahVerseData(2,
                "فَصَلِّ لِرَبِّكَ وَانْحَرْ",
                "Fasalli lirabbike venhar",
                "O hâlde Rabbin için namaz kıl ve kurban kes."),
            SurahVerseData(3,
                "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ",
                "İnne şanie'ke huvel ebter",
                "Şüphesiz sana kin güden, işte o kökü kesik olandır.")
        )
    ),

    // ── Kâfirûn (109) ───────────────────────────────────────────────────────
    Surah(
        number = 109, nameTr = "Kâfirûn", nameAr = "الكافرون", verseCount = 6,
        verses = listOf(
            SurahVerseData(1,
                "قُلْ يَا أَيُّهَا الْكَافِرُونَ",
                "Kul ya eyyühel kafirune",
                "De ki: Ey kâfirler!"),
            SurahVerseData(2,
                "لَا أَعْبُدُ مَا تَعْبُدُونَ",
                "La a'büdü ma ta'büdune",
                "Ben sizin taptıklarınıza tapmam."),
            SurahVerseData(3,
                "وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ",
                "Ve la entüm abidune ma a'büdü",
                "Siz de benim taptığıma tapıcılar değilsiniz."),
            SurahVerseData(4,
                "وَلَا أَنَا عَابِدٌ مَّا عَبَدتُّمْ",
                "Ve la ene abidün ma abedtüm",
                "Ben de sizin taptıklarınıza tapacak değilim."),
            SurahVerseData(5,
                "وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ",
                "Ve la entüm abidune ma a'büdü",
                "Siz de benim taptığıma tapacak değilsiniz."),
            SurahVerseData(6,
                "لَكُمْ دِينُكُمْ وَلِيَ دِينِ",
                "Leküm dinüküm ve liye din",
                "Sizin dininiz size, benim dinim bana.")
        )
    ),

    // ── Nasr (110) ──────────────────────────────────────────────────────────
    Surah(
        number = 110, nameTr = "Nasr", nameAr = "النصر", verseCount = 3,
        verses = listOf(
            SurahVerseData(1,
                "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ",
                "İza cae nasrullahi vel fethu",
                "Allah'ın yardımı ve fetih geldiğinde,"),
            SurahVerseData(2,
                "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا",
                "Ve raeytennase yedhulune fi dinillahi efvaca",
                "İnsanların Allah'ın dinine akın akın girdiğini gördüğünde,"),
            SurahVerseData(3,
                "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ إِنَّهُ كَانَ تَوَّابًا",
                "Fesabbih bihamdi rabbike vestağfirhu innehu kane tevvaba",
                "Rabbini hamd ile tesbih et ve O'ndan mağfiret dile. Şüphesiz O, tevbeleri çok kabul edendir.")
        )
    ),

    // ── Tebbet (111) ────────────────────────────────────────────────────────
    Surah(
        number = 111, nameTr = "Tebbet", nameAr = "المسد", verseCount = 5,
        verses = listOf(
            SurahVerseData(1,
                "تَبَّتْ يَدَا أَبِي لَهَبٍ وَتَبَّ",
                "Tebbet yeda ebi lehebin ve tebb",
                "Ebu Leheb'in iki eli kurusun! Zaten kurudu."),
            SurahVerseData(2,
                "مَا أَغْنَىٰ عَنْهُ مَالُهُ وَمَا كَسَبَ",
                "Ma ağna anhü malühu ve ma kesebe",
                "Malı ve kazandıkları ona fayda vermedi."),
            SurahVerseData(3,
                "سَيَصْلَىٰ نَارًا ذَاتَ لَهَبٍ",
                "Seyasla naren zate leheb",
                "O, alevli bir ateşe girecektir."),
            SurahVerseData(4,
                "وَامْرَأَتُهُ حَمَّالَةَ الْحَطَبِ",
                "Vemraetühu hammaletelhutab",
                "Karısı da; odun taşıyıcı olarak."),
            SurahVerseData(5,
                "فِي جِيدِهَا حَبْلٌ مِّن مَّسَدٍ",
                "Fi ciydiha hablün min mesed",
                "Boynunda hurma lifinden bükülmüş bir ip olacaktır.")
        )
    ),

    // ── İhlâs (112) ─────────────────────────────────────────────────────────
    Surah(
        number = 112, nameTr = "İhlâs", nameAr = "الإخلاص", verseCount = 4,
        verses = listOf(
            SurahVerseData(1,
                "قُلْ هُوَ اللَّهُ أَحَدٌ",
                "Kul hüvallahü ehad",
                "De ki: O, Allah Ehad'dır (tektir, eşsizdir)."),
            SurahVerseData(2,
                "اللَّهُ الصَّمَدُ",
                "Allahüssamed",
                "Allah Samed'dir; her şey O'na muhtaçtır, O hiçbir şeye muhtaç değildir."),
            SurahVerseData(3,
                "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                "Lem yelid ve lem yuled",
                "O doğurmamıştır, doğurulmamıştır."),
            SurahVerseData(4,
                "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                "Ve lem yekun lehü küfüven ehad",
                "Hiçbir şey O'nun dengi değildir.")
        )
    ),

    // ── Felak (113) ─────────────────────────────────────────────────────────
    Surah(
        number = 113, nameTr = "Felak", nameAr = "الفلق", verseCount = 5,
        verses = listOf(
            SurahVerseData(1,
                "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
                "Kul euzü birabbil felaki",
                "De ki: Sabahın Rabbine sığınırım."),
            SurahVerseData(2,
                "مِن شَرِّ مَا خَلَقَ",
                "Min şerri ma halaka",
                "Yarattığı şeylerin şerrinden,"),
            SurahVerseData(3,
                "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
                "Ve min şerri gasikin iza vekabe",
                "Bastırınca gecenin şerrinden,"),
            SurahVerseData(4,
                "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
                "Ve min şerrin neffasati fil ukadi",
                "Düğümlere üfleyen büyücülerin şerrinden,"),
            SurahVerseData(5,
                "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                "Ve min şerri hasidin iza hased",
                "Kıskandığında kıskananın şerrinden.")
        )
    ),

    // ── Nâs (114) ───────────────────────────────────────────────────────────
    Surah(
        number = 114, nameTr = "Nâs", nameAr = "الناس", verseCount = 6,
        verses = listOf(
            SurahVerseData(1,
                "قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
                "Kul euzü birabbin nasi",
                "De ki: İnsanların Rabbine sığınırım."),
            SurahVerseData(2,
                "مَلِكِ النَّاسِ",
                "Melihin nasi",
                "İnsanların melikine/hükümdarına."),
            SurahVerseData(3,
                "إِلَٰهِ النَّاسِ",
                "İlahin nasi",
                "İnsanların ilahına."),
            SurahVerseData(4,
                "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ",
                "Min şerril vesvasisil hannasi",
                "Sinsi vesvesecinin şerrinden;"),
            SurahVerseData(5,
                "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ",
                "Ellezi yüvesvisu fi sudürin nasi",
                "O ki insanların göğüslerine vesvese verir."),
            SurahVerseData(6,
                "مِنَ الْجِنَّةِ وَالنَّاسِ",
                "Minel cinneti ven nasi",
                "Cinlerden ve insanlardan.")
        )
    )
)

// ---------------------------------------------------------------------------
// Composable'lar
// ---------------------------------------------------------------------------

@Composable
fun SurelerTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SurelerHeader() }
        items(NAMAZ_SURELERI, key = { it.number }) { surah ->
            SurahCard(surah)
        }
    }
}

@Composable
private fun SurelerHeader() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Namaz Sureleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Prof. Dr. Mehmet Okuyan Meali · Ayet ayet okunuş ve anlam",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SurahCard(surah: Surah) {
    var expanded by rememberSaveable(surah.number) { mutableStateOf(false) }
    var viewMode by rememberSaveable(surah.number) { mutableStateOf(MealViewMode.MEAL) }
    var wordData by remember { mutableStateOf<Map<Int, List<QuranWord>>?>(null) }

    // Kelime Kelime moduna geçilince veri yükle
    LaunchedEffect(expanded, viewMode) {
        if (expanded && viewMode == MealViewMode.KELIME_KELIME && wordData == null) {
            wordData = try { fetchWordData(surah.number) } catch (_: Exception) { emptyMap() }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Başlık satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Green700, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(surah.number.toString(), color = Color.White,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(surah.nameTr, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text("${surah.verseCount} ayet", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(surah.nameAr, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Green700)
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Genişletilmiş içerik
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Mod seçici
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = viewMode == MealViewMode.MEAL,
                            onClick = { viewMode = MealViewMode.MEAL },
                            label = { Text("Meâl", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green700, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = viewMode == MealViewMode.KELIME_KELIME,
                            onClick = { viewMode = MealViewMode.KELIME_KELIME },
                            label = { Text("Kelime Kelime", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green700, selectedLabelColor = Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (viewMode) {
                        MealViewMode.MEAL -> SurahMealContent(surah)
                        MealViewMode.KELIME_KELIME -> SurahKelimeKelimeContent(surah, wordData)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Meâl modu: Besmele başlığı + her ayet ayrı (Arapça → Okunuş → Türkçe)
// ---------------------------------------------------------------------------

@Composable
private fun SurahMealContent(surah: Surah) {
    // Fâtiha'da besmele zaten 1. ayet; diğer surelerde ayrı başlık olarak göster
    if (surah.number != 1) {
        BesmeleHeaderCard()
        Spacer(Modifier.height(12.dp))
    }

    surah.verses.forEachIndexed { index, verse ->
        if (index > 0) Spacer(Modifier.height(10.dp))
        SurahVerseBlock(verse)
        if (index < surah.verses.lastIndex) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun BesmeleHeaderCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
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

@Composable
private fun SurahVerseBlock(verse: SurahVerseData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Ayet numarası rozeti
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Green700.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = verse.number.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Green700,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            // Arapça metin (sağdan sola)
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

            // Türkçe fonetik okunuş (yeşil arka plan kutusu)
            if (verse.transliteration.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = verse.transliteration,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Green700.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    fontStyle = FontStyle.Italic
                )
            }

            // Türkçe anlam
            if (verse.turkish.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = verse.turkish,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Kelime Kelime modu
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurahKelimeKelimeContent(surah: Surah, wordData: Map<Int, List<QuranWord>>?) {
    if (wordData.isNullOrEmpty()) {
        SurahMealContent(surah)
        return
    }
    val allWords = wordData.entries.sortedBy { it.key }.flatMap { it.value }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allWords.forEach { word -> WordColumn(word) }
        }
    }
    Spacer(Modifier.height(8.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        modifier = Modifier.padding(vertical = 4.dp))
    // Türkçe meâl referansı (tüm ayetler birleşik)
    val fullMeaning = surah.verses.joinToString("\n") { "${it.number}. ${it.turkish}" }
    Text(text = fullMeaning, style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
}
