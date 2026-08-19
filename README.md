# Hızlı Kirpi – Canlı Duvar Kağıdı

Kızın için hazırlanmış, Sonic'ten ilham alan **orijinal** (Sega'nın gerçek Sonic
görsellerini kullanmayan) bir Android canlı duvar kağıdı uygulaması. Karakter,
bulutlar, tepeler ve dönen altın halkalar tamamen kod ile (Canvas üzerinde)
çizilip her karede yeniden çizilerek animasyon oluşturuyor — bu yüzden hazır
bir GIF dosyasından daha akıcı çalışıyor ve daha az pil harcıyor.

## Kurulum (Android Studio ile)

1. [Android Studio](https://developer.android.com/studio) yükle (yoksa).
2. Android Studio'da **File > Open** ile bu klasörü (`sonic`) aç.
3. Studio ilk açılışta Gradle wrapper'ı otomatik indirip senkronize edecek
   (internet bağlantısı gerekir). "Trust Project" sorarsa onayla.
4. Kızının telefonunu USB ile bağla ve telefonda **Geliştirici Seçenekleri >
   USB Hata Ayıklama**'yı aç (Ayarlar > Telefon Hakkında'da "Yapı Numarası"na
   7 kez dokunarak geliştirici seçenekleri açılır).
5. Üstteki çalıştır (▶) düğmesine bas, telefonu hedef cihaz olarak seç.
6. Uygulama telefona kurulacak, "Hızlı Kirpi" adıyla ekrana gelecek.

## Duvar kağıdını ayarlama

1. Telefonda **Hızlı Kirpi** uygulamasını aç.
2. **"Duvar Kağıdını Ayarla"** düğmesine bas.
3. Açılan ekranda "Duvar Kağıdını Ayarla" / "Set Wallpaper" onayla.

Ekrana dokununca kirpi zıplar. Ana ekran sayfaları arasında kaydırınca
karakter hafifçe sağa/sola kayar (paralaks efekti).

## Notlar

- `minSdk = 24` (Android 7.0 ve üzeri telefonlarda çalışır).
- Görsellerin tamamı `SonicWallpaperService.kt` içinde Canvas ile çizilir,
  hiçbir telifli görsel/sprite kullanılmaz.
- Karakterin rengini, hızını veya arka plan renklerini değiştirmek için
  `app/src/main/res/values/colors.xml` ve `SonicWallpaperService.kt`
  içindeki `Paint` tanımlarını düzenlemen yeterli.
