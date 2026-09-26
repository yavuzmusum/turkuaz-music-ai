# TURKUAZ AI CORE (Backend)

Android uygulamasinin bagli oldugu merkezi servis: auth, kullanici yonetimi,
AI muzik uretim orkestrasyonu, versiyon gecmisi ve admin paneli.

## Kurulum

```bash
cd backend
python3 -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env
```

`.env` dosyasini ac ve en azindan `SECRET_KEY` degerini degistir.

### Ucretsiz AI muzik API anahtari (opsiyonel ama onerilir)

1. https://huggingface.co/join adresinden ucretsiz hesap ac.
2. https://huggingface.co/settings/tokens adresinden bir "Read" token olustur.
3. `.env` icindeki `HUGGINGFACE_API_TOKEN` degerine yapistir.

> Token girmezsen sistem otomatik olarak "placeholder" bir ton uretir
> (offline test tonu) - boylece API anahtari almadan once tum akisi
> (kayit, giris, prompt gonderme, sonuc alma, My Music, admin panel)
> uctan uca test edebilirsin. Gercek muzik kalitesi icin token gerekir.

## Calistirma

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

API dokumantasyonu: http://localhost:8000/docs

Ilk calistirmada `.env` icindeki `BOOTSTRAP_ADMIN_EMAIL` /
`BOOTSTRAP_ADMIN_PASSWORD` ile bir admin hesabi otomatik olusturulur.
Bu bilgilerle Android uygulamasinda giris yaparsan Admin Dashboard acilir.

## Android'den baglanti

Android emulator kullaniyorsan backend'e `http://10.0.2.2:8000/` adresinden
eris (emulator'un localhost'a baglanma yolu). Gercek cihazda test ederken
bilgisayarinin yerel IP adresini kullan (ayni Wi-Fi agi) ya da servisi
Railway/Render/Fly.io gibi bir platforma deploy et.

`android/app/src/main/java/.../data/api/RetrofitClient.kt` icindeki
`BASE_URL` degerini buna gore guncelle.

## Mimari notu

Android uygulamasi HICBIR gizli API anahtarini icinde barindirmaz.
Tum AI cagrilari bu backend uzerinden (server-side) yapilir; Hugging Face
(veya ileride Suno/ElevenLabs) anahtari sadece `.env` icinde, sunucuda durur.

## V1 sonrasi (bu backend'de hazir birakilan genisleme noktalari)

- `app/ai_service.py` -> `generate_from_own_library()`: ucretsiz harici API
  yetersiz kalirsa devreye alinacak kendi royalty-free kutuphane sistemi.
- `SystemSetting` tablosu: admin panelden `daily_generation_limit`,
  `maintenance_mode`, `announcement` gibi anahtarlarla yonetilir, kod
  degisikligi gerekmez.
