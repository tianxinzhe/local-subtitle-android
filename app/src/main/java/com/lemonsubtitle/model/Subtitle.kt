package com.lemonsubtitle.model

data class SubtitleLine(
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val translation: String = ""
)

data class SubtitleResult(
    val lines: List<SubtitleLine>,
    val format: SubtitleFormat
)

enum class SubtitleFormat { SRT, VTT }
