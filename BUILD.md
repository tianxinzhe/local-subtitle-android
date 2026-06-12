@echo off
echo ==^> LemonSubtitle Android Project
echo.
echo To build this project:
echo 1. Open Android Studio
echo 2. File -^> Open -^> select D:\Project_AI\local-subtitle-android
echo 3. Wait for Gradle sync
echo 4. Run on device/emulator
echo.
echo To set up whisper.cpp:
echo 1. Run: git clone https://github.com/ggerganov/whisper.cpp.git app/src/main/cpp/whisper.cpp
echo 2. Download a model from huggingface.co/ggerganov/whisper.cpp
echo 3. Place .gguf model file in app/src/main/assets/models/
echo.
echo Press any key to exit...
pause > nul
