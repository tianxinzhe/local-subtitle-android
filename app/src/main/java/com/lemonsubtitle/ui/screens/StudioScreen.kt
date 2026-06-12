package com.lemonsubtitle.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemonsubtitle.ui.theme.Success
import com.lemonsubtitle.ui.theme.Warning

data class TaskItem(
    val id: Int,
    val name: String,
    val size: String,
    val type: String,
    val status: TaskStatus,
    val progress: Int = 0
)

enum class TaskStatus { PROCESSING, WAITING, COMPLETED, FAILED }

private val sampleTasks = listOf(
    TaskItem(1, "vlog_summer_trip_01.mp4", "428.5 MB", "video", TaskStatus.PROCESSING, 45),
    TaskItem(2, "podcast_episode_12.mp3", "12.2 MB", "audio", TaskStatus.WAITING),
    TaskItem(3, "final_presentation.srt", "45 KB", "subtitle", TaskStatus.COMPLETED),
    TaskItem(4, "corrupted_video.mkv", "210 MB", "video", TaskStatus.FAILED)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen() {
    var selectedOperation by remember { mutableIntStateOf(0) }
    var selectedMode by remember { mutableIntStateOf(1) }
    val tasks = remember { sampleTasks }

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
                        .clickable { },
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
                                "+ 选择文件",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "已选择 0 个文件",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("mp4", "mkv", "wav", "srt").forEach { ext ->
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
                                Text(
                                    "+5 more",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                        "任务队列 (${tasks.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "清空",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(tasks) { task ->
                TaskCard(task)
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
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("开始全部任务", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
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
private fun TaskCard(task: TaskItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
