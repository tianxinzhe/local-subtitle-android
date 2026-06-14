package com.lemonsubtitle.model

import java.io.BufferedReader
import java.io.StringReader

object SubtitleParser {

    fun parse(content: String): SubtitleResult {
        val trimmed = content.trim()
        val format = if (trimmed.startsWith("WEBVTT")) SubtitleFormat.VTT else SubtitleFormat.SRT
        return SubtitleResult(
            lines = if (format == SubtitleFormat.VTT) parseVtt(trimmed) else parseSrt(trimmed),
            format = format
        )
    }

    private fun parseSrt(content: String): List<SubtitleLine> {
        val blocks = content.split(Regex("\\R\\R+"))
        val result = mutableListOf<SubtitleLine>()

        for (block in blocks) {
            val lines = block.trim().lines()
            if (lines.size < 2) continue

            val timestampLine = lines.find { it.contains("-->") } ?: continue
            val textLines = lines.filter { !it.matches(Regex("^\\d+$")) && !it.contains("-->") }
            if (textLines.isEmpty()) continue

            val (start, end) = parseSrtTimestamp(timestampLine) ?: continue
            val text = textLines.joinToString("\n").trim()
            result.add(SubtitleLine(startMs = start, endMs = end, text = text))
        }
        return result
    }

    private fun parseVtt(content: String): List<SubtitleLine> {
        val blocks = content.split(Regex("\\R\\R+"))
        val result = mutableListOf<SubtitleLine>()

        for (block in blocks) {
            val lines = block.trim().lines()
            val timestampLine = lines.find { it.contains("-->") } ?: continue
            val textLines = lines.filter { !it.contains("-->") && it != "WEBVTT" }
            if (textLines.isEmpty()) continue

            val (start, end) = parseVttTimestamp(timestampLine) ?: continue
            val text = textLines.joinToString("\n").trim()
            result.add(SubtitleLine(startMs = start, endMs = end, text = text))
        }
        return result
    }

    private fun parseSrtTimestamp(line: String): Pair<Long, Long>? {
        val parts = line.split("-->").map { it.trim() }
        if (parts.size < 2) return null
        val start = parseSrtTime(parts[0]) ?: return null
        val end = parseSrtTime(parts[1]) ?: return null
        return start to end
    }

    private fun parseSrtTime(time: String): Long? {
        val regex = Regex("""(\d+):(\d+):(\d+)[,.](\d+)""")
        val match = regex.find(time.trim()) ?: return null
        val (h, m, s, ms) = match.destructured
        return h.toLong() * 3600000 + m.toLong() * 60000 + s.toLong() * 1000 + ms.take(3).padEnd(3, '0').toLong()
    }

    private fun parseVttTimestamp(line: String): Pair<Long, Long>? {
        val cleaned = line.trim().replace(Regex("\\s+"), " ")
        val parts = cleaned.split("-->").map { it.trim() }
        if (parts.size < 2) return null
        val start = parseVttTime(parts[0]) ?: return null
        val end = parseVttTime(parts[1].split(Regex("\\s+")).first()) ?: return null
        return start to end
    }

    private fun parseVttTime(time: String): Long? {
        val hmsRegex = Regex("""(\d+):(\d+):(\d+)\.(\d+)""")
        val msRegex = Regex("""(\d+):(\d+)\.(\d+)""")

        hmsRegex.find(time.trim())?.let { match ->
            val (h, m, s, ms) = match.destructured
            return h.toLong() * 3600000 + m.toLong() * 60000 + s.toLong() * 1000 + ms.take(3).padEnd(3, '0').toLong()
        }
        msRegex.find(time.trim())?.let { match ->
            val (m, s, ms) = match.destructured
            return m.toLong() * 60000 + s.toLong() * 1000 + ms.take(3).padEnd(3, '0').toLong()
        }
        return null
    }

    fun formatSrtTimestamp(ms: Long): String {
        val h = ms / 3600000
        val m = (ms % 3600000) / 60000
        val s = (ms % 60000) / 1000
        val millis = ms % 1000
        return "%02d:%02d:%02d,%03d".format(h, m, s, millis)
    }

    fun formatVttTimestamp(ms: Long): String {
        val h = ms / 3600000
        val m = (ms % 3600000) / 60000
        val s = (ms % 60000) / 1000
        val millis = ms % 1000
        return "%02d:%02d:%02d.%03d".format(h, m, s, millis)
    }

    fun toSrt(lines: List<SubtitleLine>): String {
        val sb = StringBuilder()
        lines.forEachIndexed { index, line ->
            sb.appendLine(index + 1)
            sb.appendLine("${formatSrtTimestamp(line.startMs)} --> ${formatSrtTimestamp(line.endMs)}")
            sb.appendLine(line.text)
            sb.appendLine()
        }
        return sb.toString()
    }

    fun toVtt(lines: List<SubtitleLine>): String {
        val sb = StringBuilder()
        sb.appendLine("WEBVTT")
        sb.appendLine()
        for (line in lines) {
            sb.appendLine("${formatVttTimestamp(line.startMs)} --> ${formatVttTimestamp(line.endMs)}")
            sb.appendLine(line.text)
            sb.appendLine()
        }
        return sb.toString()
    }
}
