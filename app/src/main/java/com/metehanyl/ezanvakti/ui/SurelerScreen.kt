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

data class Surah(
    val number: Int,
    val nameTr: String,
    val nameAr: String,
    val verseCount: Int,
    val arabicText: String,
    val turkishMeaning: String   // Prof. Dr. Mehmet Okuyan meali
)

// ---------------------------------------------------------------------------
// Namaz Sureleri – Prof. Dr. Mehmet Okuyan Meali
// ---------------------------------------------------------------------------

val NAMAZ_SURELERI: List<Surah> = listOf(

    Surah(
        number = 1,
        nameTr = "Fâtiha",
        nameAr = "الفاتحة",
        verseCount = 7,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿١﴾ الرَّحْمَٰنِ الرَّحِيمِ ﴿٢﴾ مَالِكِ يَوْمِ الدِّينِ ﴿٣﴾ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ﴿٤﴾ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ﴿٥﴾ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ ﴿٦﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. Hamd, âlemlerin Rabbi Allah'a aittir.
2. O Rahman'dır, Rahim'dir.
3. Din gününün sahibidir.
4. Yalnız sana kulluk eder, yalnız senden yardım dileriz.
5. Bizi dosdoğru yola ilet.
6. Nimet verdiklerinin yoluna; gazaba uğrayanların ve sapkınların yoluna değil."""
    ),

    Surah(
        number = 103,
        nameTr = "Asr",
        nameAr = "العصر",
        verseCount = 3,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
وَالْعَصْرِ ﴿١﴾ إِنَّ الْإِنسَانَ لَفِي خُسْرٍ ﴿٢﴾ إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ ﴿٣﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. Asra/zamana andolsun,
2. Şüphesiz insan gerçekten hüsran içindedir.
3. Ancak iman edip salih amel işleyenler, birbirlerine hakkı tavsiye edenler ve birbirlerine sabrı tavsiye edenler bunun dışındadır."""
    ),

    Surah(
        number = 105,
        nameTr = "Fîl",
        nameAr = "الفيل",
        verseCount = 5,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ ﴿١﴾ أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ ﴿٢﴾ وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ ﴿٣﴾ تَرْمِيهِم بِحِجَارَةٍ مِّن سِجِّيلٍ ﴿٤﴾ فَجَعَلَهُمْ كَعَصْفٍ مَّأْكُولٍ ﴿٥﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. Rabbin, fil sahiplerine ne yaptı; görmedin mi?
2. Onların düzenlerini/tuzaklarını boşa çıkarmadı mı?
3. Üzerlerine sürüler hâlinde Ebabil kuşları gönderdi.
4. Onlara pişirilmiş çamurdan taşlar attırdı.
5. Böylece onları yenilmiş ekin sapı gibi ezip geçirdi."""
    ),

    Surah(
        number = 106,
        nameTr = "Kureyş",
        nameAr = "قريش",
        verseCount = 4,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
لِإِيلَافِ قُرَيْشٍ ﴿١﴾ إِيلَافِهِمْ رِحْلَةَ الشِّتَاءِ وَالصَّيْفِ ﴿٢﴾ فَلْيَعْبُدُوا رَبَّ هَٰذَا الْبَيْتِ ﴿٣﴾ الَّذِي أَطْعَمَهُم مِّن جُوعٍ وَآمَنَهُم مِّنْ خَوْفٍ ﴿٤﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. Kureyş'in yolculuklarına/ticaretlerine alıştırıldığı için,
2. Onların kış ve yaz yolculuklarına alıştırıldıkları için.
3. O hâlde bu evin Rabbine kulluk etsinler.
4. O ki onları açlıktan doyurmuş ve korkudan güvene kavuşturmuştur."""
    ),

    Surah(
        number = 107,
        nameTr = "Mâun",
        nameAr = "الماعون",
        verseCount = 7,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
أَرَأَيْتَ الَّذِي يُكَذِّبُ بِالدِّينِ ﴿١﴾ فَذَٰلِكَ الَّذِي يَدُعُّ الْيَتِيمَ ﴿٢﴾ وَلَا يَحُضُّ عَلَىٰ طَعَامِ الْمِسْكِينِ ﴿٣﴾ فَوَيْلٌ لِّلْمُصَلِّينَ ﴿٤﴾ الَّذِينَ هُمْ عَن صَلَاتِهِمْ سَاهُونَ ﴿٥﴾ الَّذِينَ هُمْ يُرَاءُونَ ﴿٦﴾ وَيَمْنَعُونَ الْمَاعُونَ ﴿٧﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. Dini/hesap gününü yalanlayanı gördün mü?
2. İşte o, yetimi şiddetle iter.
3. Yoksulun doyurulmasına teşvik etmez.
4. Vay hâline o namaz kılanlara ki,
5. Onlar namazlarından gafildirler.
6. Onlar gösteriş için yaparlar.
7. Yardımı/zekâtı da engellerler."""
    ),

    Surah(
        number = 108,
        nameTr = "Kevser",
        nameAr = "الكوثر",
        verseCount = 3,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ ﴿١﴾ فَصَلِّ لِرَبِّكَ وَانْحَرْ ﴿٢﴾ إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ ﴿٣﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. Şüphesiz biz sana Kevser'i verdik.
2. O hâlde Rabbin için namaz kıl ve kurban kes.
3. Şüphesiz sana kin güden, işte o kökü kesik olandır."""
    ),

    Surah(
        number = 109,
        nameTr = "Kâfirûn",
        nameAr = "الكافرون",
        verseCount = 6,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
قُلْ يَا أَيُّهَا الْكَافِرُونَ ﴿١﴾ لَا أَعْبُدُ مَا تَعْبُدُونَ ﴿٢﴾ وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ ﴿٣﴾ وَلَا أَنَا عَابِدٌ مَّا عَبَدتُّمْ ﴿٤﴾ وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ ﴿٥﴾ لَكُمْ دِينُكُمْ وَلِيَ دِينِ ﴿٦﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. De ki: Ey kâfirler!
2. Ben sizin taptıklarınıza tapmam.
3. Siz de benim taptığıma tapıcılar değilsiniz.
4. Ben de sizin taptıklarınıza tapacak değilim.
5. Siz de benim taptığıma tapacak değilsiniz.
6. Sizin dininiz size, benim dinim bana."""
    ),

    Surah(
        number = 110,
        nameTr = "Nasr",
        nameAr = "النصر",
        verseCount = 3,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ ﴿١﴾ وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا ﴿٢﴾ فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ إِنَّهُ كَانَ تَوَّابًا ﴿٣﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. Allah'ın yardımı ve fetih geldiğinde,
2. İnsanların Allah'ın dinine akın akın girdiğini gördüğünde,
3. Rabbini hamd ile tesbih et ve O'ndan mağfiret dile. Şüphesiz O, tevbeleri çok kabul edendir."""
    ),

    Surah(
        number = 111,
        nameTr = "Tebbet",
        nameAr = "المسد",
        verseCount = 5,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
تَبَّتْ يَدَا أَبِي لَهَبٍ وَتَبَّ ﴿١﴾ مَا أَغْنَىٰ عَنْهُ مَالُهُ وَمَا كَسَبَ ﴿٢﴾ سَيَصْلَىٰ نَارًا ذَاتَ لَهَبٍ ﴿٣﴾ وَامْرَأَتُهُ حَمَّالَةَ الْحَطَبِ ﴿٤﴾ فِي جِيدِهَا حَبْلٌ مِّن مَّسَدٍ ﴿٥﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. Ebu Leheb'in iki eli kurusun! Zaten kurudu.
2. Malı ve kazandıkları ona fayda vermedi.
3. O, alevli bir ateşe girecektir.
4. Karısı da; odun taşıyıcı olarak.
5. Boynunda hurma lifinden bükülmüş bir ip olacaktır."""
    ),

    Surah(
        number = 112,
        nameTr = "İhlâs",
        nameAr = "الإخلاص",
        verseCount = 4,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
قُلْ هُوَ اللَّهُ أَحَدٌ ﴿١﴾ اللَّهُ الصَّمَدُ ﴿٢﴾ لَمْ يَلِدْ وَلَمْ يُولَدْ ﴿٣﴾ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ ﴿٤﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. De ki: O, Allah Ehad'dır (tektir, eşsizdir).
2. Allah Samed'dir; her şey O'na muhtaçtır, O hiçbir şeye muhtaç değildir.
3. O doğurmamıştır, doğurulmamıştır.
4. Hiçbir şey O'nun dengi değildir."""
    ),

    Surah(
        number = 113,
        nameTr = "Felak",
        nameAr = "الفلق",
        verseCount = 5,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ﴿١﴾ مِن شَرِّ مَا خَلَقَ ﴿٢﴾ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ﴿٣﴾ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ﴿٤﴾ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴿٥﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. De ki: Sabahın Rabbine sığınırım.
2. Yarattığı şeylerin şerrinden,
3. Bastırınca gecenin şerrinden,
4. Düğümlere üfleyen büyücülerin şerrinden,
5. Kıskandığında kıskananın şerrinden."""
    ),

    Surah(
        number = 114,
        nameTr = "Nâs",
        nameAr = "الناس",
        verseCount = 6,
        arabicText = """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
قُلْ أَعُوذُ بِرَبِّ النَّاسِ ﴿١﴾ مَلِكِ النَّاسِ ﴿٢﴾ إِلَٰهِ النَّاسِ ﴿٣﴾ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ﴿٤﴾ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ﴿٥﴾ مِنَ الْجِنَّةِ وَالنَّاسِ ﴿٦﴾""",
        turkishMeaning = """Rahman ve Rahim olan Allah'ın adıyla.
1. De ki: İnsanların Rabbine sığınırım.
2. İnsanların melikine/hükümdarına.
3. İnsanların ilahına.
4. Sinsi vesvesecinin şerrinden;
5. O ki insanların göğüslerine vesvese verir.
6. Cinlerden ve insanlardan."""
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                text = "Prof. Dr. Mehmet Okuyan Meali",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun SurahCard(surah: Surah) {
    var expanded by rememberSaveable(surah.number) { mutableStateOf(false) }
    var viewMode by rememberSaveable(surah.number) { mutableStateOf(MealViewMode.MEAL) }
    // null = henüz yüklenmedi, emptyMap = yükleme başarısız/boş, dolu map = hazır
    var wordData by remember { mutableStateOf<Map<Int, List<QuranWord>>?>(null) }

    // Kart açıldığında kelime verisini arka planda getir (bir kez)
    LaunchedEffect(expanded) {
        if (expanded && wordData == null) {
            wordData = try {
                fetchWordData(surah.number)
            } catch (_: Exception) {
                emptyMap()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Başlık satırı — her zaman görünür, tıklanabilir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sure numarası rozeti
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Green700, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Türkçe ad + ayet sayısı
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = surah.nameTr,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${surah.verseCount} ayet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Arapça adı
                Text(
                    text = surah.nameAr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green700
                )

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Genişletilmiş içerik
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mod seçici
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = viewMode == MealViewMode.MEAL,
                            onClick = { viewMode = MealViewMode.MEAL },
                            label = { Text("Meâl", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green700,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = viewMode == MealViewMode.KELIME_KELIME,
                            onClick = { viewMode = MealViewMode.KELIME_KELIME },
                            label = { Text("Kelime Kelime", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green700,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when (viewMode) {
                        MealViewMode.MEAL -> SurahMealContent(surah, wordData)
                        MealViewMode.KELIME_KELIME -> SurahKelimeKelimeContent(surah, wordData)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Meâl modu: Arapça metin → okunuş → Türkçe anlam
// ---------------------------------------------------------------------------

@Composable
private fun SurahMealContent(surah: Surah, wordData: Map<Int, List<QuranWord>>?) {
    // Arapça metin (sağdan sola)
    Text(
        text = surah.arabicText,
        style = MaterialTheme.typography.bodyLarge.copy(
            textDirection = TextDirection.Rtl,
            textAlign = TextAlign.End,
            lineHeight = 34.sp,
            fontSize = 18.sp
        ),
        modifier = Modifier.fillMaxWidth()
    )

    // Transliterasyon — tüm ayetlerin kelimelerini birleştir
    if (!wordData.isNullOrEmpty()) {
        val translit = wordData.entries
            .sortedBy { it.key }
            .flatMap { it.value }
            .joinToString(" ") { it.transliteration }
            .trim()
        if (translit.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = translit,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.End,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(12.dp))

    // Türkçe anlam
    Text(
        text = surah.turkishMeaning,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp
    )
}

// ---------------------------------------------------------------------------
// Kelime Kelime modu: her kelimenin altında okunuş ve Türkçe anlam
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurahKelimeKelimeContent(surah: Surah, wordData: Map<Int, List<QuranWord>>?) {
    if (wordData.isNullOrEmpty()) {
        // Veri henüz yüklenmedi veya boş — normal içeriği göster
        SurahMealContent(surah, wordData)
        return
    }

    // Tüm ayetlerin kelimeleri, ayet sırasına göre
    val allWords = wordData.entries.sortedBy { it.key }.flatMap { it.value }

    // RTL düzende kelime sütunları
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allWords.forEach { word -> WordColumn(word) }
        }
    }

    Spacer(Modifier.height(6.dp))
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        modifier = Modifier.padding(vertical = 4.dp)
    )

    // Referans için Türkçe meal
    Text(
        text = surah.turkishMeaning,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 18.sp
    )
}
