#!/usr/bin/env bash
# TURKUAZ MUSIC AI - Codespaces / herhangi bir Linux ortaminda
# Android build ortamini tek seferde hazirlar.
#
# Kullanim (proje kokunde, yani turkuaz-music-ai/ icinde):
#   bash setup-android-build.sh
#
# Bu script:
#   1. SDKMAN + Gradle kurar
#   2. Android command-line tools + gerekli SDK platformlarini indirir
#   3. android/ klasorunde 'gradle wrapper' calistirarak gradlew,
#      gradlew.bat ve gradle-wrapper.jar dosyalarini DOGRU sekilde uretir
#   4. Debug APK'yi derler
#
# Isini bitirdikten sonra bundan sonraki build'ler icin sadece
# 'cd android && ./gradlew assembleDebug' yeterli olacak.

set -e

echo "== 1/4: SDKMAN ve Gradle kuruluyor =="
if [ ! -d "$HOME/.sdkman" ]; then
  curl -s "https://get.sdkman.io" | bash
fi
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle 8.7 || true

echo "== 2/4: Android SDK command-line tools kuruluyor =="
export ANDROID_HOME="$HOME/android-sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools"
cd "$ANDROID_HOME/cmdline-tools"
if [ ! -d "latest" ]; then
  curl -s -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q cmdline-tools.zip
  mv cmdline-tools latest
  rm cmdline-tools.zip
fi
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

echo "== 3/4: SDK platform ve build-tools indiriliyor (lisanslar otomatik kabul ediliyor) =="
yes | sdkmanager --licenses > /dev/null 2>&1 || true
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "== 4/4: Gradle wrapper uretiliyor ve APK derleniyor =="
cd - > /dev/null
cd android
echo "sdk.dir=$ANDROID_HOME" > local.properties
gradle wrapper --gradle-version 8.7
./gradlew assembleDebug

echo ""
echo "TAMAMLANDI. APK burada:"
echo "android/app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "Bundan sonra sadece: cd android && ./gradlew assembleDebug"
