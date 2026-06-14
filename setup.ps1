# LemonSubtitle Android - Windows Setup Script
# Run this from PowerShell in the project root

Write-Host "==> LemonSubtitle Android Dependency Setup" -ForegroundColor Green
Write-Host ""

# Step 1: Clone whisper.cpp
if (-not (Test-Path "app/src/main/cpp/whisper.cpp/.git")) {
    Write-Host "==> Cloning whisper.cpp..." -ForegroundColor Yellow
    git clone --depth 1 https://github.com/ggerganov/whisper.cpp.git app/src/main/cpp/whisper.cpp
} else {
    Write-Host "[SKIP] whisper.cpp already cloned" -ForegroundColor Cyan
}

# Step 2: Download FFmpeg prebuilt binaries
$ffmpegDir = "app/src/main/cpp/ffmpeg-android"
$ffmpegLibs = @(
    "libavformat.so", "libavcodec.so", "libavutil.so",
    "libswresample.so", "libavdevice.so", "libavfilter.so", "libswscale.so"
)
$allPresent = $true
foreach ($lib in $ffmpegLibs) {
    if (-not (Test-Path "$ffmpegDir/lib/arm64-v8a/$lib")) {
        $allPresent = $false
        break
    }
}

if (-not $allPresent) {
    Write-Host "==> FFmpeg libraries missing - downloading..." -ForegroundColor Yellow
    Write-Host "    Please download FFmpeg arm64-v8a .so files from:"
    Write-Host "    https://github.com/zhivoglas/ffmpeg-android-arm64-gpl"
    Write-Host "    Or use a prebuilt ffmpeg-android package."
    Write-Host "    Extract to: $ffmpegDir" -ForegroundColor Cyan
} else {
    Write-Host "[SKIP] FFmpeg libraries already present" -ForegroundColor Cyan
}

# Step 3: Open project in Android Studio
Write-Host ""
Write-Host "==> Opening project in Android Studio..." -ForegroundColor Green
# Try to find Android Studio
$studioPaths = @(
    "$env:LOCALAPPDATA\Programs\Android\Android Studio\bin\studio64.exe",
    "C:\Program Files\Android\Android Studio\bin\studio64.exe",
    "C:\Program Files\Android Studio\bin\studio64.exe"
)
$found = $false
foreach ($path in $studioPaths) {
    if (Test-Path $path) {
        Write-Host "    Found Android Studio at: $path"
        Start-Process -FilePath $path -ArgumentList "."
        $found = $true
        break
    }
}
if (-not $found) {
    Write-Host "    Android Studio not found. Open the project manually."
}

Write-Host ""
Write-Host "==> Setup complete!" -ForegroundColor Green
Write-Host "    After Gradle sync, build and run on an Android 8.0+ ARM64 device."
