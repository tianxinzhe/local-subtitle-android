package com.lemonsubtitle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lemonsubtitle.model.SubtitleLine
import kotlin.math.sin
import kotlin.random.Random

data class WaveformSegment(
    val startMs: Long,
    val endMs: Long,
    val amplitudes: List<Float>
)

@Composable
fun WaveformView(
    subtitleLines: List<SubtitleLine>,
    totalDurationMs: Long,
    currentTimeMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    var playheadPosition by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val waveformAmplitudes = remember(subtitleLines) {
        generateWaveformAmplitudes(totalDurationMs, subtitleLines)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .pointerInput(totalDurationMs) {
                detectTapGestures { offset ->
                    val fraction = offset.x / size.width
                    val seekMs = (fraction * totalDurationMs).toLong()
                    onSeek(seekMs)
                }
            }
            .pointerInput(totalDurationMs) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        playheadPosition = offset.x / size.width
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { _, dragAmount ->
                        val newPosition = (playheadPosition + dragAmount / size.width).coerceIn(0f, 1f)
                        playheadPosition = newPosition
                        val seekMs = (newPosition * totalDurationMs).toLong()
                        onSeek(seekMs)
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawWaveform(
                amplitudes = waveformAmplitudes,
                totalDurationMs = totalDurationMs,
                currentTimeMs = currentTimeMs,
                primaryColor = primaryColor,
                surfaceColor = surfaceColor,
                outlineColor = outlineColor
            )

            if (totalDurationMs > 0) {
                val progressFraction = (currentTimeMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                val playheadX = progressFraction * size.width

                drawLine(
                    color = primaryColor,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, size.height),
                    strokeWidth = 3f
                )

                drawCircle(
                    color = primaryColor,
                    radius = 6f,
                    center = Offset(playheadX, size.height / 2)
                )
            }

            subtitleLines.forEach { line ->
                if (totalDurationMs > 0) {
                    val startX = (line.startMs.toFloat() / totalDurationMs) * size.width
                    val endX = (line.endMs.toFloat() / totalDurationMs) * size.width

                    drawRect(
                        color = primaryColor.copy(alpha = 0.15f),
                        topLeft = Offset(startX, 0f),
                        size = Size(endX - startX, size.height)
                    )

                    drawLine(
                        color = primaryColor.copy(alpha = 0.3f),
                        start = Offset(startX, 0f),
                        end = Offset(startX, size.height),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.3f),
                        start = Offset(endX, 0f),
                        end = Offset(endX, size.height),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawWaveform(
    amplitudes: List<Float>,
    totalDurationMs: Long,
    currentTimeMs: Long,
    primaryColor: Color,
    surfaceColor: Color,
    outlineColor: Color
) {
    if (amplitudes.isEmpty()) return

    val barWidth = size.width / amplitudes.size
    val centerY = size.height / 2
    val maxAmplitude = size.height / 2 * 0.8f

    amplitudes.forEachIndexed { index, amplitude ->
        val x = index * barWidth
        val barHeight = amplitude * maxAmplitude
        val progressFraction = if (totalDurationMs > 0) currentTimeMs.toFloat() / totalDurationMs else 0f
        val barFraction = x / size.width
        val isPlayed = barFraction < progressFraction

        val color = if (isPlayed) primaryColor else surfaceColor

        drawRect(
            color = color,
            topLeft = Offset(x + 1, centerY - barHeight / 2),
            size = Size(barWidth.coerceAtLeast(2f) - 2, barHeight)
        )
    }
}

private fun generateWaveformAmplitudes(
    totalDurationMs: Long,
    subtitleLines: List<SubtitleLine>
): List<Float> {
    val sampleCount = 200
    val amplitudes = mutableListOf<Float>()

    for (i in 0 until sampleCount) {
        val timeMs = (i.toFloat() / sampleCount) * totalDurationMs
        val isSubtitleRegion = subtitleLines.any { line ->
            timeMs >= line.startMs && timeMs <= line.endMs
        }

        val baseAmplitude = if (isSubtitleRegion) {
            0.3f + Random.nextFloat() * 0.5f
        } else {
            0.05f + Random.nextFloat() * 0.15f
        }

        val sineWave = sin(i * 0.3f) * 0.1f
        amplitudes.add((baseAmplitude + sineWave).coerceIn(0.05f, 1f))
    }

    return amplitudes
}
