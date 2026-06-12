package com.lemonsubtitle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Database
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lemonsubtitle.ui.theme.Success

@Composable
fun ModelManagerScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("模型管理", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "管理已导入的语音识别模型",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { }) { Icon(Icons.Default.Search, null) }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "已导入模型",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "1 个模型",
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Database, null,
                        tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Whisper small", style = MaterialTheme.typography.titleLarge)
                    Text("466MB • GGUF 格式", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Delete, null,
                        tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("推荐下载", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground)
                Row(
                    modifier = Modifier.clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("查看 HuggingFace",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.OpenInNew, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            listOf(
                Triple("Whisper tiny", "75MB • 极速识别，低准确率", false),
                Triple("Whisper base", "142MB • 均衡速度与效果", false),
                Triple("Whisper small", "466MB • 高准确率，推荐使用", true)
            ).forEach { (name, desc, imported) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, style = MaterialTheme.typography.titleLarge)
                        Text(desc, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (imported) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = Success, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("已就绪", style = MaterialTheme.typography.labelLarge,
                                color = Success)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = { },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.UploadFile, null,
                    tint = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text("手动导入模型", style = MaterialTheme.typography.titleLarge)
                Text("支持 .bin 或 .gguf 格式的 Whisper 模型文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = { },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("选择文件")
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}
