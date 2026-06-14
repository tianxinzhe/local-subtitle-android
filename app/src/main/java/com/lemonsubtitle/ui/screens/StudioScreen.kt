package com.lemonsubtitle.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.navigation.NavController
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemonsubtitle.service.ProcessingManager
import com.lemonsubtitle.ui.navigation.Screen
import com.lemonsubtitle.ui.theme.Success

data class TaskItem(
    val id: Int,
    val name: String,
    val size: String,
    val type: String,
    val status: TaskStatus,
    val progress: Int = 0
)

enum class TaskStatus { PROCESSING, WAITING, COMPLETED, FAILED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedOperation by remember { mutableIntStateOf(0) }
    var selectedMode by remember { mutableIntStateOf(1) }
    var selectedFiles by remember { mutableStateOf<List<SelectedFile>>(emptyList()) }
    var processing by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedFiles = uris.mapNotNull { uri -> getFileInfo(context, uri) }
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
        }
    }

    var tasks by remember(selectedFiles) {
        mutableStateOf(
            if (selectedFiles.isEmpty()) {
                listOf(
                    TaskItem(0, "vlog_summer_trip_01.mp4", "428.5 MB", "video", TaskStatus.WAITING),
                    TaskItem(1, "podcast_episode_12.mp3", "12.2 MB", "audio", TaskStatus.WAITING),
                )
            } else selectedFiles.mapIndexed { index, file ->
                TaskItem(
                    id = index,
                    name = file.name,
                    size = formatSize(file.size),
                    type = file.type,
                    status = TaskStatus.WAITING
                )
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("柠檬字幕工作室") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "快速操作",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "查看全部",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OperationCard("提取音频", Icons.Default.AudioFile, MaterialTheme.colorScheme.primary, selectedOperation == 0) {
                        selectedOperation = 0
                    }
                    OperationCard("转字幕", Icons.Default.Movie, MaterialTheme.colorScheme.secondary, selectedOperation == 1) {
                        selectedOperation = 1
                    }
                    OperationCard("翻译字幕", Icons.Default.Translate, MaterialTheme.colorScheme.tertiary, selectedOperation == 2) {
                        selectedOperation = 2
                    }
                    OperationCard("自动流水线", Icons.Default.Bolt, MaterialTheme.colorScheme.primaryContainer, selectedOperation == 3) {
                        selectedOperation = 3
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clickable {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "video/mp4", "video/x-matroska", "video/quicktime",
                                    "audio/mpeg", "audio/wav", "audio/mp4", "audio/ogg",
                                    "text/plain", "text/vtt"
                                )
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (selectedFiles.isEmpty()) "+ 选择文件" else "已选择 ${selectedFiles.size} 个文件",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                if (selectedFiles.isEmpty()) "点击选择视频、音频或字幕文件"
                                else "点击继续添加更多文件",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("mp4", "mkv", "mov", "mp3", "wav", "srt", "vtt").forEach { ext ->
                                    Text(
                                        ext.uppercase(),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "任务队列 (${tasks.toList().size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (selectedFiles.isNotEmpty()) {
                        IconButton(onClick = { selectedFiles = emptyList() }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "清空",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(tasks.toList()) { task ->
                TaskCard(
                    task = task,
                    onClick = {
                        if (selectedFiles.isNotEmpty() && task.type == "subtitle") {
                            val file = selectedFiles.getOrNull(task.id)
                            if (file != null) {
                                navController?.navigate(
                                    Screen.SubtitleEdit.createRoute(file.uri.toString())
                                )
                            }
                        }
                    }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("全局配置", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(12.dp))

                    Text("识别模式", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        listOf("快速", "平衡", "高质量").forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = selectedMode == index,
                                onClick = { selectedMode = index },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = 3
                                )
                            ) {
                                Text(label)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    FilledTonalButton(
                        onClick = {
                            if (selectedFiles.isNotEmpty() && !processing) {
                                processing = true
                                scope.launch {
                                    val fileList = selectedFiles.toList()
                                    for ((i, file) in fileList.withIndex()) {
                                        tasks = tasks.toMutableList().also { it[i] = it[i].copy(status = TaskStatus.PROCESSING) }
                                        val result = ProcessingManager.processFile(
                                            context = context,
                                            fileUri = file.uri,
                                            fileName = file.name,
                                            fileType = file.type,
                                            onProgress = { progress ->
                                                tasks = tasks.toMutableList().also {
                                                    it[i] = it[i].copy(
                                                        progress = (progress.progress * 100).toInt()
                                                    )
                                                }
                                            }
                                        )
                                        tasks = tasks.toMutableList().also {
                                            it[i] = it[i].copy(
                                                status = if (result.success) TaskStatus.COMPLETED else TaskStatus.FAILED,
                                                progress = if (result.success) 100 else 0
                                            )
                                        }
                                    }
                                    processing = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !processing
                    ) {
                        Text(
                            if (processing) "处理中..." else "开始全部任务",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

private data class SelectedFile(
    val uri: Uri,
    val name: String,
    val size: Long,
    val type: String
)

private fun getFileInfo(context: Context, uri: Uri): SelectedFile? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    return cursor.use {
        if (it.moveToFirst()) {
            val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
            val name = if (nameIdx >= 0) it.getString(nameIdx) else uri.lastPathSegment ?: "Unknown"
            val size = if (sizeIdx >= 0 && it.getLong(sizeIdx) >= 0) it.getLong(sizeIdx) else 0L
            val type = when {
                name.endsWith(".srt", true) || name.endsWith(".vtt", true) -> "subtitle"
                name.endsWith(".mp3", true) || name.endsWith(".wav", true) || name.endsWith(".m4a", true) || name.endsWith(".ogg", true) -> "audio"
                else -> "video"
            }
            SelectedFile(uri = uri, name = name, size = size, type = type)
        } else null
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "Unknown"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIdx = 0
    while (value >= 1024 && unitIdx < 3) {
        value /= 1024
        unitIdx++
    }
    return if (unitIdx > 0) "%.1f %s".format(value, units[unitIdx])
    else "%.0f %s".format(value, units[unitIdx])
}

@Composable
private fun OperationCard(
    label: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else iconColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TaskCard(task: TaskItem, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                TaskStatus.PROCESSING -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (task.status) {
                                TaskStatus.PROCESSING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                TaskStatus.COMPLETED -> Success.copy(alpha = 0.1f)
                                TaskStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (task.type) {
                            "video" -> Icons.Default.Movie
                            "audio" -> Icons.Default.AudioFile
                            else -> Icons.Default.AudioFile
                        },
                        contentDescription = null,
                        tint = when (task.status) {
                            TaskStatus.COMPLETED -> Success
                            TaskStatus.FAILED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitle = when (task.status) {
                        TaskStatus.PROCESSING -> "${task.size} • 正在转录..."
                        TaskStatus.WAITING -> "${task.size} • 等待中"
                        TaskStatus.COMPLETED -> "${task.size} • 已完成"
                        TaskStatus.FAILED -> "文件损坏"
                    }
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (task.status) {
                            TaskStatus.FAILED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                when (task.status) {
                    TaskStatus.PROCESSING -> Text(
                        "${task.progress}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TaskStatus.COMPLETED -> Icon(Icons.Default.CheckCircle, null,
                        tint = Success, modifier = Modifier.size(20.dp))
                    TaskStatus.FAILED -> Icon(Icons.Default.Replay, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (task.status == TaskStatus.PROCESSING) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { task.progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
