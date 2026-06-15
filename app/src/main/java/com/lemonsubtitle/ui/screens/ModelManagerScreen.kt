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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Database
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lemonsubtitle.data.DownloadStatus
import com.lemonsubtitle.data.ImportedModel
import com.lemonsubtitle.data.ModelDownloadManager
import com.lemonsubtitle.data.ModelManager
import com.lemonsubtitle.ui.theme.Success
import com.lemonsubtitle.ui.theme.Warning
import kotlinx.coroutines.launch

@Composable
fun ModelManagerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var models by remember { mutableStateOf(listOf<ImportedModel>()) }
    var showDeleteDialog by remember { mutableStateOf<ImportedModel?>(null) }
    val downloadProgress by ModelDownloadManager.progress.collectAsState()

    LaunchedEffect(Unit) {
        models = ModelManager.listModels(context)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        for (uri in uris) {
            ModelManager.importModel(context, uri).onSuccess {
                models = ModelManager.listModels(context)
            }
        }
    }

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
                    Icon(Icons.Default.Search, null)
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
                    "${models.size} 个模型",
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(12.dp))

            if (models.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无已导入的模型",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                models.forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                            Text(model.name, style = MaterialTheme.typography.titleLarge)
                            Text("${model.sizeMb} • GGUF 格式",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showDeleteDialog = model }) {
                            Icon(Icons.Default.Delete, null,
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
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

            ModelDownloadManager.availableModels.forEach { modelInfo ->
                val progressData = downloadProgress[modelInfo.id]
                val isDownloaded = models.any { it.name == modelInfo.name }
                val isDownloading = progressData?.status == DownloadStatus.DOWNLOADING
                val isValidating = progressData?.status == DownloadStatus.VALIDATING
                val isFailed = progressData?.status == DownloadStatus.FAILED
                val isCompleted = progressData?.status == DownloadStatus.COMPLETED

                ModelDownloadCard(
                    modelInfo = modelInfo,
                    isDownloaded = isDownloaded,
                    progressData = progressData,
                    onDownload = {
                        scope.launch {
                            ModelDownloadManager.downloadModel(context, modelInfo) { }
                            models = ModelManager.listModels(context)
                        }
                    },
                    onCancel = {
                        ModelDownloadManager.cancelDownload(modelInfo.id)
                    },
                    onRetry = {
                        ModelDownloadManager.resetProgress(modelInfo.id)
                        scope.launch {
                            ModelDownloadManager.downloadModel(context, modelInfo) { }
                            models = ModelManager.listModels(context)
                        }
                    }
                )
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
                Text("支持 .gguf 格式的 Whisper 模型文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("选择文件")
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }

    showDeleteDialog?.let { model ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除模型") },
            text = { Text("确定删除「${model.name}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    ModelManager.deleteModel(context, model)
                    models = ModelManager.listModels(context)
                    showDeleteDialog = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ModelDownloadCard(
    modelInfo: com.lemonsubtitle.data.ModelInfo,
    isDownloaded: Boolean,
    progressData: com.lemonsubtitle.data.DownloadProgress?,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    val isDownloading = progressData?.status == DownloadStatus.DOWNLOADING
    val isValidating = progressData?.status == DownloadStatus.VALIDATING
    val isFailed = progressData?.status == DownloadStatus.FAILED
    val isCompleted = progressData?.status == DownloadStatus.COMPLETED
    val showProgress = isDownloading || isValidating

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(modelInfo.name, style = MaterialTheme.typography.titleLarge)
                Text("${modelInfo.sizeMb} • ${modelInfo.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when {
                isDownloaded || isCompleted -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = Success, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("已就绪", style = MaterialTheme.typography.labelLarge,
                            color = Success)
                    }
                }
                isFailed -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = onRetry) {
                            Text("重试", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                showProgress -> {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("取消")
                    }
                }
                else -> {
                    FilledTonalButton(
                        onClick = onDownload,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        if (showProgress && progressData != null) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressData.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isValidating) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    isValidating -> "校验文件格式..."
                    progressData.totalBytes > 0 -> {
                        val downloadedMb = "%.1f".format(progressData.downloadedBytes / (1024.0 * 1024.0))
                        val totalMb = "%.1f".format(progressData.totalBytes / (1024.0 * 1024.0))
                        "$downloadedMb / $totalMb MB • ${(progressData.progress * 100).toInt()}%"
                    }
                    else -> "下载中..."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isFailed && progressData?.error?.isNotEmpty() == true) {
            Spacer(Modifier.height(4.dp))
            Text(
                progressData.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
