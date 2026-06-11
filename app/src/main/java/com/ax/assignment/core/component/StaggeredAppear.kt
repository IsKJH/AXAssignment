package com.ax.assignment.core.component

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val STEP_MS = 50L
private const val APPEAR_MS = 300
// Rows composed within this window after the anchor belong to the initial
// viewport; anything composed later (scroll, period swipe) appears instantly
private const val INITIAL_WINDOW_MS = 120L

/** Anchor for stagger slots — call once where the list content first composes. */
@Composable
fun rememberEntranceTime(): Long = remember { SystemClock.uptimeMillis() }

/**
 * Shared list-row entrance: rows fade in and float up one after another
 * (50ms/row), used by the home, statistics and category lists so every
 * screen greets content the same way.
 */
@Composable
fun StaggeredAppear(
    index: Int,
    entranceTime: Long,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val playEntrance = remember {
        SystemClock.uptimeMillis() - entranceTime < INITIAL_WINDOW_MS
    }
    val appear = remember { Animatable(if (playEntrance) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (playEntrance) {
            val wait = entranceTime + index * STEP_MS - SystemClock.uptimeMillis()
            if (wait > 0) delay(wait)
            appear.animateTo(1f, tween(APPEAR_MS))
        }
    }
    Box(
        modifier = modifier.graphicsLayer {
            alpha = appear.value
            translationY = (1f - appear.value) * 20.dp.toPx()
        },
    ) { content() }
}
