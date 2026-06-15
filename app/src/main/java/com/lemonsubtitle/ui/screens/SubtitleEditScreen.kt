package com.lemonsubtitle.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.lemonsubtitle.model.SubtitleLine
import com.lemonsubtitle.model.SubtitleParser
import com.lemonsubtitle.service.TranslationHelper
import com.lemonsubtitle.ui.components.VideoPlayer
import com.lemonsubtitle.ui.components.WaveformView
import kotlinx.coroutines.launch

private val sampleSubtitles = listOf(
    SubtitleLine(250000, 254500, "这是一个带有电影质感的日本风景空镜。", "This is a cinematic empty shot of Japanese scenery."),
    SubtitleLine(254500, 258200, "远处的富士山在晨曦中若隐若现。"),
    SubtitleLine(258200, 262000, "这种光影效果给画面增添了层次感。"),
    SubtitleLine(262000, 266150, "欢迎来到柠檬字幕工作室的教程频道。"),
    SubtitleLine(266150, 270000, "今天我们来学习如何制作专业的双语字幕。"),
    SubtitleLine(270000, 274500, "首先你需要准备一段带有对白的视频素材。"),
)

@Composable
fun SubtitleEditScreen(fileUri: String = "", onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExportDialog by remember { mutableStateOf(false) }
    var showBilingualPreview by remember { mutableStateOf(true) }
    var subtitles by remember { mutableStateOf(sampleSubtitles) }
    var selectedIndex by remember { mutableStateOf(-1) }
    var isLoading by remember { mutableStateOf(fileUri.isNotEmpty()) }
    var exportFormat by remember { mutableStateOf("srt") }
    var activeTab by remember { mutableStateOf(0) }
    var currentTimeMs by remember { mutableLongStateOf(0L) }
    var translatingIndex by remember { mutableStateOf(-1) }

    val totalDurationMs = remember(subtitles) {
        subtitles.maxOfOrNull { it.endMs } ?: 300000L
    }

    val videoUri = remember(fileUri) {
        if (fileUri.isNotEmpty()) Uri.parse(fileUri) else null
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            try {
                val content = when (exportFormat) {
                    "srt" -> SubtitleParser.toSrt(subtitles)
                    "vtt" -> SubtitleParser.toVtt(subtitles)
                    "ass" -> SubtitleParser.toAss(subtitles)
                    else -> SubtitleParser.toSrt(subtitles)
                }
                context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(fileUri) {
        if (fileUri.isNotEmpty()) {
            try {
                val uri = Uri.parse(fileUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.readText() ?: ""
                inputStream?.close()
                if (content.isNotBlank()) {
                    val result = SubtitleParser.parse(content)
                    subtitles = result.lines
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("柠檬字幕工作室", style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.History, null) }
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            "PREVIEW MODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "双语预览",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = showBilingualPreview,
                                onCheckedChange = { showBilingualPreview = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
                FilledTonalButton(
                    onClick = { showExportDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("导出字幕")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("加载中...", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                VideoPlayer(
                    videoUri = videoUri,
                    modifier = Modifier
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "编辑",
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (activeTab == 0) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainer
                                )
                                .clickable { activeTab = 0 }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (activeTab == 0) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "波形",
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (activeTab == 1) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainer
                                )
                                .clickable { activeTab = 1 }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (activeTab == 1) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        IconButton(onClick = { }) { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        IconButton(onClick = { }) { Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }

                if (activeTab == 1) {
                    WaveformView(
                        subtitleLines = subtitles,
                        totalDurationMs = totalDurationMs,
                        currentTimeMs = currentTimeMs,
                        onSeek = { currentTimeMs = it },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(subtitles) { index, subtitle ->
                        val isSelected = index == selectedIndex
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(12.dp)
                                    ) else Modifier.border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                                )
                                .clickable {
                                    selectedIndex = index
                                    currentTimeMs = subtitle.startMs
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    formatTimestamp(subtitle.startMs, subtitle.endMs),
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            translatingIndex = index
                                            scope.launch {
                                                val translated = TranslationHelper.translate(
                                                    subtitle.text,
                                                    "zh",
                                                    "en"
                                                )
                                                subtitles = subtitles.toMutableList().also {
                                                    it[index] = it[index].copy(translation = translated)
                                                }
                                                translatingIndex = -1
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        if (translatingIndex == index) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Translate, null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ContentCopy, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp).clickable { }
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            var editText by remember(subtitle) { mutableStateOf(subtitle.text) }
                            BasicTextField(
                                value = editText,
                                onValueChange = { editText = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (showBilingualPreview && subtitle.translation.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    subtitle.translation,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showExportDialog) {
        var selectedFormat by remember { mutableStateOf(0) }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("导出格式", modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SRT (标准格式)", "VTT (Web格式)", "ASS (特效字幕)").forEachIndexed { i, label ->
                        Button(
                            onClick = { selectedFormat = i },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(label, modifier = Modifier.weight(1f))
                            if (selectedFormat == i) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("取消", color = MaterialTheme.colorScheme.primary)
                    }
                    FilledTonalButton(onClick = {
                        showExportDialog = false
                        val extension = when (selectedFormat) {
                            0 -> ".srt"
                            1 -> ".vtt"
                            else -> ".ass"
                        }
                        exportFormat = when (selectedFormat) {
                            0 -> "srt"
                            1 -> "vtt"
                            else -> "ass"
                        }
                        exportLauncher.launch("subtitles$extension")
                    }) {
                        Text("开始导出")
                    }
                }
            }
        )
    }
}

private fun formatTimestamp(startMs: Long, endMs: Long): String {
    fun pad(n: Long): String = n.toString().padStart(2, '0')
    fun fmt(ms: Long): String {
        val h = ms / 3600000
        val m = (ms % 3600000) / 60000
        val s = (ms % 60000) / 1000
        val mi = ms % 1000
        return "${pad(h)}:${pad(m)}:${pad(s)}.${mi.toString().padStart(3, '0')}"
    }
    return "${fmt(startMs)} — ${fmt(endMs)}"
}
