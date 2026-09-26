"""
TURKUAZ AI CORE - Muzik uretim servisi.

V1 stratejisi:
- Ucretsiz katman: Hugging Face Inference API uzerinden Meta'nin acik kaynak
  MusicGen modelini kullaniyoruz (facebook/musicgen-small). Hugging Face,
  bu modeller icin ucretsiz bir Inference API sunuyor (rate-limit'li).
  Token: https://huggingface.co/settings/tokens adresinden ucretsiz alinir.
- Ileride: eger ucretli/daha kaliteli bir API'ye (Suno, ElevenLabs Music vb.)
  gecmek istenirse, sadece bu dosyadaki generate_music() fonksiyonu
  degistirilir; geri kalan sistem (Android, DB, admin panel) etkilenmez.
- Alternatif: kendi muzik kutuphanemizi olusturmak istersek, bu fonksiyon
  yerine promptu analiz edip kendi ses/loop kutuphanemizden esleyen bir
  algoritma yazilabilir (bkz. dosya sonundaki generate_from_own_library iskeleti).
"""
import io
import os
import uuid
import wave
import struct
import math
from pathlib import Path

import httpx

from app.config import settings

HF_API_URL = f"https://api-inference.co/models/{settings.musicgen_model}"
# Not: gercek endpoint https://api-inference.huggingface.co/models/{model}
HF_API_URL = f"https://api-inference.huggingface.co/models/{settings.musicgen_model}"


class MusicGenerationError(Exception):
    pass


def _media_dir() -> Path:
    p = Path(settings.media_dir)
    p.mkdir(parents=True, exist_ok=True)
    return p


async def generate_music(prompt: str, duration_seconds: int = 15) -> str:
    """
    Prompt'u Hugging Face MusicGen API'sine gonderir, donen ses dosyasini
    diske kaydeder ve dosya yolunu dondurur.

    HUGGINGFACE_API_TOKEN ayarlanmamissa, gelistirme/test amacli olarak
    yerinde (offline) bir placeholder ton uretir; boylece backend token
    olmadan da uctan uca test edilebilir.
    """
    if not settings.huggingface_api_token:
        return _generate_placeholder_tone(prompt, duration_seconds)

    headers = {"Authorization": f"Bearer {settings.huggingface_api_token}"}
    payload = {
        "inputs": prompt,
        "parameters": {"duration": duration_seconds},
    }

    try:
        async with httpx.AsyncClient(timeout=120) as client:
            response = await client.post(HF_API_URL, headers=headers, json=payload)

        if response.status_code == 503:
            # Model soguk baslangicta yukleniyor olabilir; Hugging Face bu durumda 503 doner.
            raise MusicGenerationError(
                "AI modeli su an yukleniyor, lutfen birkac saniye sonra tekrar deneyin."
            )
        if response.status_code != 200:
            raise MusicGenerationError(f"Muzik uretim servisi hata dondu: {response.status_code}")

        audio_bytes = response.content
        filename = f"{uuid.uuid4()}.wav"
        filepath = _media_dir() / filename
        filepath.write_bytes(audio_bytes)
        return str(filepath)

    except httpx.RequestError as exc:
        raise MusicGenerationError(f"AI servisine baglanilamadi: {exc}")


def _generate_placeholder_tone(prompt: str, duration_seconds: int) -> str:
    """
    Gelistirme modu: gercek bir AI API'ye baglanmadan, prompt'un hash'ine
    gore basit bir ton/akor dizisi uretip WAV olarak kaydeder. Sadece
    backend/Android akisini API anahtari olmadan test edebilmek icindir.
    """
    sample_rate = 22050
    n_samples = sample_rate * duration_seconds
    seed = sum(ord(c) for c in prompt) or 1
    base_freq = 110 + (seed % 220)  # 110-330 Hz araliginda bir kok nota

    filename = f"{uuid.uuid4()}.wav"
    filepath = _media_dir() / filename

    with wave.open(str(filepath), "w") as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        frames = bytearray()
        for i in range(n_samples):
            t = i / sample_rate
            # basit bir akor (kok + kvint) + yavas fade-out, "placeholder" oldugunu belirtmek icin
            value = 0.3 * math.sin(2 * math.pi * base_freq * t)
            value += 0.2 * math.sin(2 * math.pi * base_freq * 1.5 * t)
            fade = max(0.0, 1 - (t / duration_seconds))
            sample = int(value * fade * 32767)
            frames += struct.pack("<h", sample)
        wf.writeframes(frames)

    return str(filepath)


def generate_from_own_library(prompt: str, duration_seconds: int) -> str:
    """
    ILERIDE: ucretsiz harici API bulunamazsa/yetersiz kalirsa devreye
    alinacak, kendi royalty-free loop/sample kutuphanemizden prompt'a en
    uygun parcalari secip birlestiren bir sistem icin iskelet.
    Su an implement edilmedi.
    """
    raise NotImplementedError("Kendi muzik kutuphanesi V1 sonrasi icin planlaniyor.")
