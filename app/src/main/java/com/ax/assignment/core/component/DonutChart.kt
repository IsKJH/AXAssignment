package com.ax.assignment.core.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.CardPrimary
import com.ax.assignment.core.theme.CategoryColors
import com.ax.assignment.core.theme.TrackGray
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun DonutChart(
    segments: List<Pair<Color, Float>>,
    modifier: Modifier = Modifier,
    diameter: Dp = 136.dp,
    strokeWidth: Dp = 26.dp,
    emptyColor: Color = TrackGray,
    centerDiameter: Dp = diameter - strokeWidth * 2,
    // Called with the segment index while the finger is down (scrubbing updates live),
    // and with null when the finger lifts
    onSegmentPress: ((Int?) -> Unit)? = null,
    centerContent: @Composable () -> Unit = {},
) {
    // Sweep-in animation: segments draw clockwise from the top on (re)entry
    val sweepProgress = remember { Animatable(0f) }
    LaunchedEffect(segments) {
        sweepProgress.snapTo(0f)
        sweepProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    // Press/scrub on the ring band → resolve the segment under the finger
    // (clockwise from 12 o'clock); release reports null
    val scrubModifier = if (onSegmentPress != null) {
        Modifier.pointerInput(segments) {
            fun hitSegment(pos: Offset): Int? {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val dx = pos.x - cx
                val dy = pos.y - cy
                val dist = hypot(dx, dy)
                val outerR = size.width / 2f
                val innerR = outerR - strokeWidth.toPx()
                if (dist !in innerR..outerR) return null
                val angle = (Math.toDegrees(atan2(dy, dx).toDouble()) + 90 + 360) % 360
                val total = segments.sumOf { it.second.toDouble() }.toFloat()
                if (total <= 0f) return null
                var acc = 0f
                for ((index, seg) in segments.withIndex()) {
                    if (seg.second <= 0f) continue
                    acc += seg.second / total * 360f
                    if (angle <= acc) return index
                }
                return null
            }
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val first = hitSegment(down.position) ?: return@awaitEachGesture
                down.consume()
                onSegmentPress(first)
                drag(down.id) { change ->
                    hitSegment(change.position)?.let { onSegmentPress(it) }
                    change.consume()
                }
                onSegmentPress(null)
            }
        }
    } else Modifier

    Box(
        modifier = modifier.size(diameter).then(scrubModifier),
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
        // Figma 488:898 — inner white disc (1dp #EEE border) floating inside the ring hole
        Box(
            modifier = Modifier
                .size(centerDiameter)
                .background(Color.White, CircleShape)
                .border(1.dp, Color(0xFFEEEEEE), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            centerContent()
        }
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
