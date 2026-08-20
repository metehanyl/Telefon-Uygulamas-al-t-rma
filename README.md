# Şifre Kasası

Basit, tamamen cihaz üzerinde çalışan (internet gerektirmeyen) bir Android şifre
yöneticisi. Üye olduğun sitelerin **kısa adını**, **site uzantısını/adresini**,
**kullanıcı adını** ve **şifreni** kaydedebilirsin. Şifreler cihazının donanım
destekli Android Keystore'unda tutulan AES-256-GCM anahtarıyla şifrelenerek
veritabanında saklanır; uygulamayı açarken bir ana şifre (master password)
istenir.

## Özellikler

- İlk açılışta ana şifre belirleme, sonraki açılışlarda kilit ekranı
- Site adı (kısa ad), site uzantısı (domain), kullanıcı adı, şifre kaydı
- Kayıtlı şifreleri arama
- Şifreyi göster/gizle, panoya kopyalama
- Şifre AES-256-GCM ile Android Keystore anahtarıyla şifrelenir (anahtar
  cihazdan asla çıkmaz)
- Uygulama arka plana alındığında otomatik kilitlenir
- `android:allowBackup="false"` — bulut yedeğiyle taşınamayan bir anahtara
  bağlı olduğu için yedekleme kapalı

## Nasıl kurulur

**En kolay yol — APK'yı GitHub Releases'ten indir:**
Bu depoya her push yapıldığında GitHub Actions otomatik olarak APK'yı derler
ve deponun **Releases** sayfasında `apk-latest` etiketiyle yayınlar. Telefonunun
tarayıcısından o sayfadaki `app-debug.apk` dosyasına dokunup indir, ardından
"bilinmeyen kaynaklardan yükleme" izni istendiğinde onayla ve kur.

**Kaynak koddan derlemek istersen:**
1. Bu klasörü Android Studio ile aç.
2. Gradle senkronizasyonunun bitmesini bekle.
3. `Build > Build Bundle(s) / APK(s) > Build APK(s)` ile derle, ya da
   `./gradlew assembleDebug` komutunu çalıştır.
4. Üretilen APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Proje yapısı

```
app/src/main/java/com/metehanyil/sifrekasasi/
├── crypto/     CryptoManager (AES-GCM, Keystore) ve MasterPasswordManager (PBKDF2 kilit)
├── data/       Room: PasswordEntry, PasswordDao, AppDatabase
└── ui/         LockActivity, MainActivity, AddEditActivity, PasswordAdapter
```

## Güvenlik notları

- Şifreleme anahtarı Android Keystore'da tutulur; ana şifre yalnızca uygulamaya
  giriş kilididir, şifreleme anahtarını türetmez. Bu sayede ana şifreyi
  değiştirmek kayıtlı verileri bozmaz.
- Uygulama tamamen çevrimdışı çalışır, herhangi bir sunucuya veri göndermez
  (manifest'te `INTERNET` izni bile yoktur).
- Telefonunu factory reset yaparsan veya uygulamayı kaldırıp yeniden
  kurarsan, Keystore anahtarı da silinir ve eski kayıtlar geri getirilemez —
  bu tasarım gereği (anahtar cihaza bağlıdır).
