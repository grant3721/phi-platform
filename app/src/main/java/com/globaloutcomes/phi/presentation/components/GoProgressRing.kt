package com.globaloutcomes.phi.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.globaloutcomes.phi.presentation.theme.GoAmber
import com.globaloutcomes.phi.presentation.theme.GoGreen
import com.globaloutcomes.phi.presentation.theme.GoGrayLight
import com.globaloutcomes.phi.presentation.theme.GoRed

/**
 * GO Progress Ring - Circular progress indicator
 * Used for daily scan cap tracking (scans today / 50 max)
 */
@Composable
fun GoProgressRing(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    showLabel: Boolean = true,
    animated: Boolean = true
) {
    val progress = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)

    // Color based on progress (green → amber → red)
    val progressColor = when {
        progress < 0.7f -> GoGreen
        progress < 0.9f -> GoAmber
        else -> GoRed
    }

    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) progress else progress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Background ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = GoGrayLight,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Progress ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Center label
        if (showLabel) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$current",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
                Text(
                    text = "/ $max",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Simple circular progress indicator (indeterminate)
 */
@Composable
fun GoLoadingRing(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = GoGreen
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = strokeWidth
        )
    }
}
