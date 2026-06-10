package com.ax.assignment.core.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.CardPrimary
import com.ax.assignment.core.theme.CategoryColors
import com.ax.assignment.core.theme.TrackGray

@Composable
fun DonutChart(
    segments: List<Pair<Color, Float>>,
    modifier: Modifier = Modifier,
    diameter: Dp = 136.dp,
    strokeWidth: Dp = 26.dp,
    emptyColor: Color = TrackGray,
    centerContent: @Composable () -> Unit = {},
) {
    // Sweep-in animation: segments draw clockwise from the top on (re)entry
    val sweepProgress = remember { Animatable(0f) }
    LaunchedEffect(segments) {
        sweepProgress.snapTo(0f)
        sweepProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = Stroke(width = strokeWidth.toPx())
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx())
            val topLeft = Offset(inset, inset)

            val totalWeight = segments.sumOf { it.second.toDouble() }.toFloat()
            if (totalWeight <= 0f || segments.isEmpty()) {
                drawArc(
                    color = emptyColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                return@Canvas
            }

            val visibleAngle = 360f * sweepProgress.value
            var startAngle = -90f
            var consumed = 0f
            segments.forEach { (color, weight) ->
                if (weight <= 0f) return@forEach
                val sweep = weight / totalWeight * 360f
                val remaining = visibleAngle - consumed
                if (remaining <= 0f) return@forEach
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep.coerceAtMost(remaining),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                startAngle += sweep
                consumed += sweep
            }
        }
        centerContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPreview() {
    AXAssignmentTheme {
        DonutChart(
            segments = listOf(
                CardPrimary to 0.5f,
                CategoryColors[0] to 0.3f,
                CategoryColors[1] to 0.2f,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartEmptyPreview() {
    AXAssignmentTheme {
        DonutChart(segments = emptyList())
    }
}
