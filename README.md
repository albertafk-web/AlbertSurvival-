# AlbertSurvival

**Gelişmiş Susuzluk ve Vücut Sıcaklığı Sistemi**  
Paper / Purpur 26.3 için Minecraft Java Edition eklentisi.

## Özellikler

- **Susuzluk (0–100)**
  - Koşma, zıplama, sıcak biyom, Nether, yağmur, yüzme etkileri
  - Su şişesi, süt, bal şişesi ile yenileme
  - Kritik seviyede hasar + yavaşlama
  - Creative / Spectator istisnaları
  - Permission bypass

- **Vücut Sıcaklığı (-100 … +100)**
  - Biyom, hava durumu, günün saati, yükseklik
  - Zırh koruması (leather soğukta güçlü)
  - Ateş, kamp ateşi, lav, buz, kar yakını etki
  - Aşamalı donma ve aşırı ısınma hasarı
  - Yumuşak geçiş (change-rate)

- **HUD**
  - BossBar (Susuzluk + Sıcaklık)
  - ActionBar değer gösterimi
  - **Önemli:** Vanilla açlık barının hemen üstüne özel ikon çizmek pure server-side mümkün değildir. Tam referans görünüm için client mod gerekir.

- Kalıcı YAML oyuncu verisi
- Tam Türkçe config ve mesajlar
- Reload güvenli
- Performans odaklı (40 tick aralık, tarama limiti)

## Gereksinimler

- Paper veya Purpur **26.3**
- Java 21+

## Kurulum

1. `AlbertSurvival-1.0.0.jar` dosyasını `plugins/` klasörüne koyun.
2. Sunucuyu başlatın.
3. `plugins/AlbertSurvival/config.yml` dosyasını ihtiyacınıza göre düzenleyin.
4. `/albertsurvival reload` ile yeniden yükleyin.

### Aternos Uyarısı

**Aternos ücretsiz servisinde özel JAR yüklemesi desteklenmez.**  
Sadece Aternos eklenti kataloğundaki eklentiler yüklenebilir.  
Özel plugin için:
- [exaroton](https://exaroton.com) (Aternos ekibinin ücretli platformu)
- Kendi VPS / dedicated Paper sunucunuz

## Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/albertsurvival` | Yardım / sürüm | - |
| `/albertsurvival reload` | Config yenile | `albertsurvival.reload` |
| `/albertsurvival status [oyuncu]` | Durum göster | `albertsurvival.status` |
| `/albertsurvival setthirst <oyuncu> <0-100>` | Susuzluk ayarla | `albertsurvival.setthirst` |
| `/albertsurvival settemperature <oyuncu> <-100..100>` | Sıcaklık ayarla | `albertsurvival.settemperature` |
| `/albertsurvival reset [oyuncu]` | Verileri sıfırla | `albertsurvival.reset` |
| `/albertsurvival debug` | Debug aç/kapa | `albertsurvival.debug` |
| `/albertsurvival hud` | HUD bilgisi | `albertsurvival.hud` |

## Yetkiler

- `albertsurvival.admin` – tüm yetkiler
- `albertsurvival.bypass.thirst`
- `albertsurvival.bypass.temperature`
- `albertsurvival.bypass.damage`

## Derleme

```bash
cd AlbertSurvival
./gradlew build
```

JAR: `build/libs/AlbertSurvival-1.0.0.jar`

**Not:** Paper API 26.3.build.xxx sürümü değişebilir. `build.gradle.kts` içindeki versiyonu güncelleyin.

## HUD Sınırlaması (Teknik)

Paper API istemci HUD’una özel sprite çizemez.  
Bu eklenti BossBar + ActionBar kullanır.  
Referans ekran görüntüsündeki “açlık barının üstünde damla + termometre” görünümü için:

1. Resource pack + custom font (sınırlı)
2. Fabric/Quilt client mod (önerilen)
3. Iris + shader (gelişmiş)

Sunucu tarafı mekanikler (hasar, azalma, kayıt) tam çalışır.

## Lisans

MIT
