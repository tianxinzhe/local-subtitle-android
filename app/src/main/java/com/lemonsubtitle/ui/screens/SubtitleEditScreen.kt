package com.lemonsubtitle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class SubtitleLine(
    val start: String,
    val end: String,
    val text: String,
    val translation: String = ""
)

private val sampleSubtitles = listOf(
    SubtitleLine("00:04:10.000", "00:04:14.500", "这是一个带有电影质感的日本风景空镜。", "This is a cinematic empty shot of Japanese scenery."),
    SubtitleLine("00:04:14.500", "00:04:18.200", "远处的富士山在晨曦中若隐若现。"),
    SubtitleLine("00:04:18.200", "00:04:22.000", "这种光影效果给画面增添了层次感。"),
    SubtitleLine("00:04:22.000", "00:04:26.150", "欢迎来到柠檬字幕工作室的教程频道。"),
    SubtitleLine("00:04:26.150", "00:04:30.000", "今天我们来学习如何制作专业的双语字幕。"),
    SubtitleLine("00:04:30.000", "00:04:34.500", "首先你需要准备一段带有对白的视频素材。"),
)

@Composable
fun SubtitleEditScreen() {
    var showExportDialog by remember { mutableStateOf(false) }
    var showBilingualPreview by remember { mutableStateOf(true) }
    var subtitles by remember { mutableStateOf(sampleSubtitles) }
    var selectedIndex by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("柠檬字幕工作室", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { }) {
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
                    Text(
                        "双语预览",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(64.dp).clickable { }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.33f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                            Icon(Icons.Default.SkipNext, null, tint = Color.White)
                            Icon(Icons.Default.VolumeUp, null, tint = Color.White)
                            Text(
                                "00:04:12 / 00:12:34",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Default.ClosedCaption, null, tint = Color.White)
                            Icon(Icons.Default.Fullscreen, null, tint = Color.White)
                        }
                    }
                }
            }

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
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "波形",
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = { }) { Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
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
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${subtitle.start} — ${subtitle.end}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.Default.ContentCopy,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp).clickable { }
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        var editText by remember { mutableStateOf(subtitle.text) }
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
                                Icon(Icons.Default.Download, null,
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
                    FilledTonalButton(onClick = { showExportDialog = false }) {
                        Text("开始导出")
                    }
                }
            }
        )
    }
}
