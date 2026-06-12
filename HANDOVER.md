# 柠檬字幕工作室 Android 版 — 项目交接文档

> 生成日期：2026-06-12
> 仓库地址：https://github.com/tianxinzhe/local-subtitle-android

---

## 一、项目概况

一款 Android 离线字幕处理 App，功能对标 Windows 版《柠檬字幕工作室》：
- 视频→音频提取
- 音频→字幕（Whisper.cpp）
- 字幕翻译（ML Kit）
- 字幕编辑+双语导出
- 批量后台流水线

**技术栈：**
- Kotlin + Jetpack Compose + Material 3
- NDK + CMake（whisper.cpp、ffmpeg-android）
- Google ML Kit Translate（离线翻译）
- WorkManager + ForegroundService（后台任务）
- DataStore（配置持久化）

**Android 版本：**
- minSdk: 26（Android 8.0）
- targetSdk: 35
- ABI: arm64-v8a

---

## 二、已完成的开发工作

### 2.1 需求文档（3 个文件）
| 文件 | 内容 |
|:--|:--|
| `spec.md` | 完整功能需求（FR-1~FR-4，AC-1~AC-21） |
| `tasks.md` | 15 个实现 Task（P0/P1/P2 优先级） |
| `checklist.md` | 验证清单（8 个分类，60+ 验证项） |

**关键功能决策（已定稿）：**
- 4 个底部导航页面：串联工作室 / 字幕编辑 / 模型管理 / 设置
- "提取音频"+"转字幕"+"翻译字幕"+"自动流水线"全部放在串联工作室主页面
- Whisper 模型用户自行下载 .gguf 文件，通过应用导入
- 翻译首发用 ML Kit，后期支持自定义模型
- 语言自动识别，用户可手动修正
- 不做音频波形图（第一版）
- GPU 任务开始前检测一次，运行中不热切换
- 导出格式：MP3/WAV/AAC/OGG + SRT/VTT

### 2.2 UI 设计（Google Stitch 输出）
| 目录 | 页面 | 设计图 |
|:--|:--|:--|
| `stitch/_1/` | 串联工作室 | `screen.png` + `code.html` |
| `stitch/_2/` | 字幕编辑 | `screen.png` + `code.html` |
| `stitch/_3/` | 设置 | `screen.png` + `code.html` |
| `stitch/_4/` | 模型管理 | `screen.png` + `code.html` |
| `stitch/lemonsubtitle_precision/DESIGN.md` | 设计系统（颜色、排版、间距、组件规范） | - |

### 2.3 已实现的代码

**项目骨架：**
- `settings.gradle.kts` / `build.gradle.kts` / `gradle.properties`
- `gradle/libs.versions.toml` — 版本统一管理

**App 模块：**
- `app/build.gradle.kts` — 依赖配置（Compose、ML Kit、Navigation、DataStore）
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml` — 权限 + 组件注册

**主题系统（完全按 DESIGN.md 配色）：**
- `ui/theme/Color.kt` — 暗色/亮色完整色板
- `ui/theme/Type.kt` — 排版定义
- `ui/theme/Theme.kt` — Material 3 Dark/Light 主题

**导航：**
- `ui/navigation/Screen.kt` — 路由定义
- `ui/navigation/AppNavigation.kt` — 底部导航 4 tab + NavHost

**4 个页面（UI 完成，逻辑为空）：**
- `ui/screens/StudioScreen.kt` — 操作卡片+文件选择器+任务列表+配置栏
- `ui/screens/SubtitleEditScreen.kt` — 视频播放器+字幕列表+导出对话框
- `ui/screens/ModelManagerScreen.kt` — 已导入模型+推荐下载+手动导入
- `ui/screens/SettingsScreen.kt` — 设置列表+品牌卡片

**多语言：**
- `res/values/strings.xml` — 英文（fallback 默认）
- `res/values-zh/strings.xml` — 中文

**NDK 基础：**
- `cpp/CMakeLists.txt` — CMake 配置，关联 whisper.cpp 和 ffmpeg-android
- `cpp/whisper_bridge.cpp` — JNI 桥接（只有空函数骨架）
- `WhisperBridge.kt` — Kotlin 侧 JNI 声明

**后台服务：**
- `service/ProcessingService.kt` — ForegroundService 骨架

---

## 三、尚未实现的功能（需要后续开发）

### 优先级 P0（核心链路）

| # | 功能 | 对应 Task | 说明 |
|:--|:--|:--|:--|
| 1 | **文件选择器** | Task 4 | 用 `ActivityResultContracts.OpenMultipleDocuments()` 实现，支持 mp4/mkv/mov/m4a/mp3/wav/srt/vtt |
| 2 | **ffmpeg 提取音频** | Task 5 | JNI 调用 ffmpeg，参数 `-vn -ar 16000 -ac 1 -c:a pcm_s16le`，导出 MP3/WAV/AAC/OGG |
| 3 | **Whisper.cpp 转写** | Task 6 | 实现 `whisper_bridge.cpp` 的 init/transcribe/release，从 GGUF 模型路径加载 |
| 4 | **字幕解析器** | Task 7 | Kotlin 实现 SRT/VTT 解析，兼容断档行号、样式代码、人名标记 |
| 5 | **ML Kit 翻译** | Task 8 | 调用 Translation API，`translate.setSourceLanguage().setTargetLanguage()` |
| 6 | **任务队列引擎** | Task 11 | WorkManager 链式任务 + ForegroundService 通知 |
| 7 | **字幕导出** | Task 9/13 | 生成 SRT/VTT 文件，通过 SAF 保存到用户选择目录 |
| 8 | **异常处理** | Task 12 | 60 秒超时、磁盘满检测、时间轴重叠检测 |

### 优先级 P1

| # | 功能 | 对应 Task | 说明 |
|:--|:--|:--|:--|
| 9 | **模型导入** | Task 10 | 从文件选择器选择 .gguf 文件，复制到 `filesDir/models/`，注册到模型列表 |
| 10 | **多语言运行时切换** | Task 13 | `AppCompatDelegate.setApplicationLocales()` |
| 11 | **设置持久化** | Task 14 | DataStore 保存默认语种、模型、输出目录 |

### 优先级 P2

| # | 功能 | 说明 |
|:--|:--|:--|
| 12 | Whisper 语言自动检测+手动修正 | 用户可覆盖自动识别的语言 |
| 13 | 双语合成预览 | 编辑器中显示原文+译文对照 |
| 14 | 导出格式选择弹窗 | SRT/VTT 选择器（UI 已有但逻辑空） |

---

## 四、推荐接下来的开发顺序

```
第1步: 文件选择器（获取文件 URI）→ 实现后可测试文件选取
    ↓
第2步: ffmpeg 提取音频 → 实现后可将视频转 WAV
    ↓
第3步: Whisper.cpp 转写（JNI 实现）→ 实现后可将音频转文本
    ↓
第4步: 字幕解析器（SRT/VTT）→ 解析 whisper 输出为字幕模型
    ↓
第5步: 在编辑器中显示字幕 → 打通核心链路
    ↓
第6步: ML Kit 翻译 + 字幕导出 → MVP 完成
    ↓
第7步: 任务队列 + 批量处理 + 异常处理
```

---

## 五、需准备的环境

1. **whisper.cpp 源码**：clone 到 `app/src/main/cpp/whisper.cpp/`
2. **ffmpeg-android 预编译库**：下载 arm64-v8a 版本放到 `app/src/main/cpp/ffmpeg-android/`
3. **Whisper GGUF 模型**：从 HuggingFace 下载（推荐 ggml-tiny.bin 起步，约 75MB）
4. **Android Studio Ladybug (2024.2+)**
5. **Gradle 8.9**（wrapper 已配置）
6. **测试设备**：Android 8.0+, ARM64, 建议 4GB+ 内存

---

## 六、关键参考

### 架构说明
- 所有 UI 使用 Jetpack Compose，没有 XML 布局
- 后端计算全部在 NDK C++ 层（whisper/ffmpeg）
- 翻译用 Google ML Kit，无需本地模型
- 线程模型：UI 用 Coroutine，NDK 计算放在 `Default` dispatcher
- 文件访问用 `ContentResolver` + SAF，不直接读路径
- 模型文件存 `context.filesDir/models/`，临时文件存 `context.cacheDir`

### 颜色方案（来自 DESIGN.md）
- 主色 Primary：`#1A73E8`（Google Blue）
- 强调色 Secondary：`#F1C501`（柠檬黄）
- 暗色背景：`#131313`，卡片：`#201F1F`
- 亮色背景：`#FFFFFF`，卡片：`#F5F5F5`

### 导航结构
```
BottomNavigationBar
├── Studio（默认首页）
├── Subtitle Edit
├── Model Manager
└── Settings
```
