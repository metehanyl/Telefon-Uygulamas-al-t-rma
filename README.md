# Ezan Vakti

Konumuna göre namaz vakitlerini gösteren, internet olmadığında en son
çekilen vakitleri gösteren ve her vakit girdiğinde bildirim gönderen bir
Android uygulaması.

## Özellikler

- Diyanet verilerini topluluk aynası olan `ezanvakti.emushaf.net` üzerinden çeker
  (şehir → ilçe → vakit zinciri, Diyanet'in resmi API'si genel kullanıma açık
  olmadığı için bu aynayı kullanır).
- Telefonun GPS/ağ konumunu kullanarak otomatik olarak bulunduğun şehir ve
  ilçeyi tespit eder (Google Play Services gerektirmez, yalnızca Android'in
  yerleşik `LocationManager` ve `Geocoder` sınıflarını kullanır).
- Şehir/ilçe otomatik tespit edilemezse, listeden manuel seçim yapılabilir.
- İnternet yoksa veya günün verisi çekilemiyorsa, cihazda saklanan en son
  vakitler gösterilir ("çevrimdışı"/"güncel değil" uyarısıyla birlikte).
- Her vakit girdiğinde bildirim gönderir (sistemin varsayılan bildirim
  sesiyle — telif/lisans kısıtları nedeniyle gerçek ezan sesi uygulamaya
  gömülmemiştir). Bildirimler `AlarmManager` ile tam zamanlı olarak
  planlanır ve telefon yeniden başlatıldığında otomatik olarak yeniden
  kurulur.
- Diğer uygulamaların (örn. Çalar Saat) o günün İmsak vaktini okuyabilmesi
  için salt-okunur bir `ContentProvider` sağlar
  (`content://com.metehanyl.ezanvakti.provider/imsak`). Bu sadece cihazda
  zaten önbelleğe alınmış vakti döner; veri çekmez, bu yüzden okuyan
  uygulamadan önce bu uygulamanın en az bir kez açılıp vakitlerin
  güncellenmiş olması gerekir.

## Projeyi açma ve APK oluşturma

Bu depo, derlenmiş bir APK içermez — Android Studio gerektiren tam bir
kaynak kod projesidir. Kurulum:

1. [Android Studio](https://developer.android.com/studio) güncel bir sürümünü
   kur (Gradle 8.7 ve AGP 8.5.2 ile uyumlu bir sürüm; "Koala" veya sonrası
   önerilir).
2. Android Studio'da **File → Open** ile bu depo klasörünü aç.
3. Açılışta Gradle senkronizasyonu otomatik başlar; ilk senkronizasyon
   internet bağlantısı gerektirir (Gradle dağıtımı ve bağımlılıklar
   indirilecektir).
4. Senkronizasyon bittikten sonra üst menüden **Build → Build Bundle(s) /
   APK(s) → Build APK(s)** seçeneğini kullanarak `app-debug.apk` dosyasını
   oluştur, ya da bir cihaz/emülatör bağlayıp yeşil **Run** düğmesine bas.
5. Oluşan APK `app/build/outputs/apk/debug/app-debug.apk` yolunda olacaktır;
   bu dosyayı telefonuna kopyalayıp (veya doğrudan Android Studio üzerinden
   "Run" ile) kurabilirsin. Bilinmeyen kaynaklardan kurulum izni gerekebilir.

## İzinler

Uygulama ilk açıldığında şu izinleri ister:

- **Konum** (kaba/hassas): otomatik şehir/ilçe tespiti için.
- **Bildirim** (Android 13+): vakit bildirimleri göstermek için.
- **Tam zamanlı alarm** (Android 12+): vakit bildirimlerinin dakikası
  dakikasına gelmesi için; izin verilmezse sistem bildirimleri biraz
  geciktirebilir.

Konum izni verilmezse veya konum tespit edilemezse, sağ üstteki konum
simgesinden şehir/ilçe manuel olarak seçilebilir.

## Bilinmesi gerekenler / sınırlamalar

- Bu proje, ağ erişimi kısıtlı bir ortamda hazırlandığından, gerçek bir
  Android SDK + Gradle derlemesiyle uçtan uca test edilememiştir. Kaynak
  kodu dikkatlice gözden geçirilmiş ve bağımsız bir Kotlin derleyici
  geçişiyle sözdizimi açısından doğrulanmıştır, ancak Android Studio'da
  ilk açılışta küçük bağımlılık/uyumluluk düzeltmeleri gerekebilir.
- `ezanvakti.emushaf.net` resmi olmayan bir topluluk aynasıdır; servis
  kesintisi durumunda uygulama önbellekteki son vakitleri göstermeye devam
  eder ama yeni veri çekemez.
- Gerçek ezan sesi dosyası uygulamaya dahil edilmemiştir; bildirimler
  sistemin varsayılan bildirim sesini kullanır.
