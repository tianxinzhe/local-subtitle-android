# LemonSubtitle Android - Setup Script
# Run this script from the project root to download dependencies

echo "==> Creating directory structure..."
mkdir -p app/src/main/cpp/whisper.cpp
mkdir -p app/src/main/cpp/ffmpeg-android

echo "==> Downloading whisper.cpp..."
git clone --depth 1 https://github.com/ggerganov/whisper.cpp.git app/src/main/cpp/whisper.cpp

echo "==> Downloading whisper models..."
mkdir -p app/src/main/assets/models/
wget -q https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin -O app/src/main/assets/models/ggml-tiny.bin

echo "==> Done! Open the project in Android Studio."
