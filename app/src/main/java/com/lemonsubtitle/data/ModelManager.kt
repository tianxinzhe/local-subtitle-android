package com.lemonsubtitle.data

import android.content.Context
import android.net.Uri
import java.io.File

data class ImportedModel(
    val name: String,
    val path: String,
    val sizeMb: String
)

object ModelManager {

    private const val MODELS_DIR = "models"

    fun getModelsDir(context: Context): File {
        return File(context.filesDir, MODELS_DIR).also { it.mkdirs() }
    }

    fun listModels(context: Context): List<ImportedModel> {
        val dir = getModelsDir(context)
        return dir.listFiles()
            ?.filter { it.name.endsWith(".gguf") || it.name.endsWith(".bin") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                val sizeMb = "%.0fMB".format(file.length() / (1024.0 * 1024.0))
                ImportedModel(
                    name = file.nameWithoutExtension,
                    path = file.absolutePath,
                    sizeMb = sizeMb
                )
            } ?: emptyList()
    }

    fun importModel(context: Context, uri: Uri): Result<ImportedModel> = runCatching {
        val fileName = uri.lastPathSegment ?: "model_${System.currentTimeMillis()}.gguf"
        val cleanName = fileName.substringAfterLast('/').substringAfterLast('\\')
        val targetFile = File(getModelsDir(context), cleanName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw RuntimeException("Cannot read file")

        val sizeMb = "%.0fMB".format(targetFile.length() / (1024.0 * 1024.0))
        ImportedModel(
            name = cleanName.removeSuffix(".gguf").removeSuffix(".bin"),
            path = targetFile.absolutePath,
            sizeMb = sizeMb
        )
    }

    fun deleteModel(context: Context, model: ImportedModel) {
        File(model.path).delete()
    }
}
