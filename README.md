# TURKUAZ MUSIC AI — V1

## İçerik

```
turkuaz-music-ai/
├── backend/     TURKUAZ AI CORE (Python FastAPI) - auth, kullanıcı, AI orkestrasyon, admin
└── android/     Android uygulaması (Kotlin + Jetpack Compose)
```

## Hızlı başlangıç

**1. Backend'i çalıştır** (detaylar `backend/README.md`):
```bash
cd backend
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```
Token girmesen de sistem test için otomatik "placeholder" ses üretir — yani
Hugging Face hesabı açmadan önce tüm akışı (kayıt, login, prompt→müzik,
My Music, admin panel) uçtan uca deneyebilirsin.

**2a. Android Studio ile (PC varsa)**: `android/` klasörünü Android Studio
ile aç, Gradle sync'in bitmesini bekle, emülatörde çalıştır (emülatör
backend'e otomatik olarak `10.0.2.2:8000` üzerinden ulaşır).

**2b. PC olmadan (GitHub Codespaces ile)**: Repoyu GitHub'da aç → Code →
Codespaces → Create codespace. Açılan terminalde proje kökünde şunu çalıştır:
```bash
bash setup-android-build.sh
```
Bu script Gradle + Android SDK'yı kurar, `gradlew`'i doğru şekilde üretir ve
APK'yı derler. Sonuç: `android/app/build/outputs/apk/debug/app-debug.apk`.
Bu dosyayı Codespace dosya panelinden sağ tık → Download ile telefona indirip
kurabilirsin. (Not: `gradlew` ve `gradle-wrapper.jar` bilerek repoya
eklenmedi — derlenmiş bir binary olduğu için üretim ortamında script
tarafından doğru şekilde oluşturuluyor, elle eklenen sahte bir dosya build'i
kırardı.)

**3. Admin girişi**: `.env`'deki `BOOTSTRAP_ADMIN_EMAIL` /
`BOOTSTRAP_ADMIN_PASSWORD` ile giriş yaparsan direkt Admin Dashboard açılır.

## Spec'e göre neyin nerede olduğu

| Spec bölümü | Karşılığı |
|---|---|
| 3. Giriş/kayıt, admin ayrımı | `AuthScreen.kt`, backend `auth.py` (admin backend'de doğrulanır) |
| 4. Home | `HomeScreen.kt` |
| 5. Create (tek prompt → müzik) | `CreateScreen.kt` → backend `POST /music` → `ai_service.py` |
| 6. Müzik sonucu (player) | `ResultScreen.kt` (ExoPlayer) |
| 7. My Music + filtreler + menü | `MyMusicScreen.kt` |
| 8. AI ile tekrar düzenleme, versiyonlar | `EditScreen.kt` → backend `POST /music/{id}/edit` |
| 9. Admin Panel | `admin/` klasörü + backend `admin.py` |
| 10. TURKUAZ AI CORE mimarisi | `backend/` (Android hiçbir API anahtarı barındırmaz) |

## Neyin gerçek, neyin iskelet olduğu (dürüst özet)

- **Gerçek ve çalışır durumda**: auth (JWT + bcrypt), veritabanı modelleri,
  versiyon geçmişi mantığı, admin yetki kontrolü (backend'de), günlük üretim
  limiti, bakım modu, tüm Android ekranları ve navigasyon akışı.
- **Token'sız çalışan test modu**: `HUGGINGFACE_API_TOKEN` boşsa gerçek AI
  yerine basit bir "placeholder" ton üretilir — akışı test etmek için,
  gerçek müzik kalitesi için değil.
- **Henüz yapılmadı / V1 sonrası**: kendi royalty-free müzik kütüphanesi
  (`generate_from_own_library` iskelet halde), gerçek dosya indirme (Android
  tarafında `DownloadManager` entegrasyonu eklenmeli), raporlanan içerik
  moderasyon UI'ı, backend'i bir sunucuya deploy etme.

## Sıradaki adım için öneri

Bunu gerçekten telefonunda görmek için en hızlı yol: backend'i lokal
çalıştır, Android Studio'da projeyi aç ve emülatörde başlat. İstersen bir
sonraki adımda birlikte Hugging Face token'ını alıp gerçek AI üretimini
devreye sokabiliriz, ya da bir bulut sağlayıcıya (Railway/Render) deploy
edip gerçek cihazdan da erişilebilir hale getirebiliriz.
