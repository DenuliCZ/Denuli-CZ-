package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.util.WaveformHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun WaveformRenderer(
    filePath: String?,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF00FFCC), // Cyberpunk neon green/cyan
    inactiveColor: Color = Color(0xFF261D45), // Deep purple/slate background
    isMuted: Boolean = false,
    playProgress: Float = 0f // Current audio play progress (0.0 to 1.0)
) {
    var rawWaveformPoints by remember(filePath) { mutableStateOf<List<Float>>(emptyList()) }
    
    // Load waveform points asynchronously on Dispatchers.IO to maintain 60FPS list rendering
    LaunchedEffect(filePath) {
        if (!filePath.isNullOrEmpty()) {
            val file = File(filePath)
            if (file.exists()) {
                val points = withContext(Dispatchers.IO) {
                    WaveformHelper.getWaveform(file, numPoints = 64)
                }
                rawWaveformPoints = points
            } else {
                rawWaveformPoints = emptyList()
            }
        } else {
            rawWaveformPoints = emptyList()
        }
    }

    // Dynamic wave bar animation for visual live feedback
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_waveform")
    val waveScaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "oscillator"
    )

    Box(modifier = modifier.fillMaxWidth().height(48.dp)) {
        if (rawWaveformPoints.isEmpty()) {
            // Draw placeholder ambient flat line when no sound data exists
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midY = size.height / 2f
                drawLine(
                    color = inactiveColor.copy(alpha = 0.5f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        } else {
            // Draw real canvas-based multi-bar waveform with a stunning gradient look
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount = rawWaveformPoints.size
                val totalWidth = size.width
                val totalHeight = size.height
                val midY = totalHeight / 2f

                // Spacing logic compliant with standard modern grid
                val barSpacing = 3.dp.toPx()
                val totalSpacing = barSpacing * (barCount - 1)
                val barWidth = (totalWidth - totalSpacing) / barCount

                val activeBrush = Brush.horizontalGradient(
                    colors = if (isMuted) {
                        listOf(Color(0xFF8E8CA4), Color(0xFF5D5870))
                    } else {
                        listOf(activeColor.copy(alpha = 0.85f), activeColor)
                    }
                )

                for (i in 0 until barCount) {
                    val peak = rawWaveformPoints[i]
                    // Scale peak value slightly depending on active animation
                    val animatedPeak = (peak * waveScaleFactor).coerceIn(0.05f, 1.0f)
                    val barHeight = totalHeight * animatedPeak * 0.85f
                    val xOffset = i * (barWidth + barSpacing)
                    val yOffset = midY - (barHeight / 2f)

                    // Check if this bar has been passed by the track playProgress index
                    val isPast = (i.toFloat() / barCount) <= playProgress

                    val currentColor = if (isPast) {
                        if (isMuted) Color(0xFF8E8CA4) else activeColor
                    } else {
                        inactiveColor.copy(alpha = 0.7f)
                    }

                    drawRoundRect(
                        color = currentColor,
                        topLeft = Offset(xOffset, yOffset),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
        }
    }
}
